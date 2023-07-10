package com.github.lucky44x.gui;


import com.github.lucky44x.gui.abstraction.GUI;
import com.github.lucky44x.gui.components.PagedArray;
import com.github.lucky44x.luckyutil.config.LangConfig;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.*;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

/**
 * The GUI parent Class for all File-Based GUIs
 *
 * __NOTE: CALL finishInit() after finishing Sub-Class constructors
 *
 * @author Nick Balischewski
 */
public abstract class FileGUI extends GUI {

    // region DataHolder
    private static final HashMap<String, JsonObject> files = new HashMap<String, JsonObject>();
    // endregion

    // region Inventory Vars
    private InventoryView view;
    private InventoryType type;
    private int size;
    private final String fileName;
    private final LangConfig langFile;
    private boolean allowShiftClick = false;
    // endregion

    // region Reflection vars
    private final HashMap<String, methodCarrier> taggedMethods = new HashMap<>();
    private final HashMap<String, methodCarrier> taggedItemGenerators = new HashMap<>();
    private final HashMap<String, fieldCarrier> taggedFields = new HashMap<>();
    private final HashMap<String, Integer> taggedSlots = new HashMap<>();
    private final Object[] extensions;
    // endregion

    // region Inventory Construction vars
    private JsonObject parentObject;
    private final HashMap<String, ItemStack> generatedItems = new HashMap<>();
    private final HashMap<Integer, methodCarrier> registeredListeners = new HashMap<>();
    private final LinkedList<slotCarrier> slotsToSet = new LinkedList<>();
    private final HashMap<String, String> textReplacements = new HashMap<>();
    // endregion

    /**
     * Creates a File-GUI instance for the given GUI file
     * @param instance The Main Plugin instance
     * @param user The User who is going to use the GUI
     * @param fileName The GUI's filename (with or without .json)
     * @param langFile a {@link FileConfiguration}FileConfiguration instance which holds the 'LANG-File'
     * @throws FileNotFoundException Gets thrown when there is no file corresponding to the given name
     */
    public FileGUI(Plugin instance, Player user, String fileName, LangConfig langFile) throws FileNotFoundException {
        super(
                instance,
                user,
                "FILE \"" + instance.getDataFolder() + "\\LuckyGUI\\"
                        + (fileName.endsWith(".json") ? fileName : fileName + ".json") + " NOT FOUND");

        this.langFile = langFile;
        this.fileName = fileName;
        extensions = null;
    }

    /**
     * Creates a new File-GUI-Instance for the given GUI-File
     * @param instance The Main Plugin instance
     * @param user The user who is going to use the GUI
     * @param fileName The GUI's filename
     * @param langFile The {@link com.github.lucky44x.luckyutil.config.LangConfig LangConfig} Instance which will be used to get text with the "LANG:lang_id" syntax
     * @param extensions The Array of instances of Objects which hold custom logic for extending the functionality of File-GUIs
     * @throws FileNotFoundException Gets thrown when there is no file corresponding to the given file-name
     */
    public FileGUI(Plugin instance, Player user, String fileName, LangConfig langFile, Object[] extensions)
            throws FileNotFoundException {
        super(
                instance,
                user,
                "FILE \"" + instance.getDataFolder() + "\\LuckyGUI\\"
                        + (fileName.endsWith(".json") ? fileName : fileName + ".json") + " NOT FOUND");

        this.langFile = langFile;
        this.fileName = fileName;
        this.extensions = extensions;
    }

    @Override
    protected final void onLoad() {
        try {
            reflectObject(this);
            if (extensions != null) {
                for (Object o : extensions) {
                    reflectObject(o);
                }
            }
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        parentObject = loadGUIFile(fileName, instance);

        // region reading Inventory Vars from gui-file
        title = getText(parentObject.get("title").getAsString());
        type = InventoryType.valueOf(parentObject.get("type").getAsString());

        if (parentObject.has("allow-shift-click"))
            allowShiftClick = parentObject.get("allow-shift-click").getAsBoolean();

        if (type == InventoryType.CHEST) size = parentObject.get("size").getAsInt();
        else size = 0;

        readItems();
        // region Extension Reading
        if (parentObject.has("other")) {
            JsonArray extensionArray = parentObject.get("other").getAsJsonArray();
            readStaticExtensions(extensionArray);
        }
        // endregion
    }

    private void readStaticExtensions(JsonArray extensionArray) {
        for (int i = 0; i < extensionArray.size(); i++) {
            JsonObject extensionObject = extensionArray.get(i).getAsJsonObject();

            String type = extensionObject.get("type").getAsString();
            switch (type.toUpperCase()) {
                case ("PAGED-ARRAY") -> {
                    String name = extensionObject.get("name").getAsString();
                    String genTag = extensionObject.get("generator-tag").getAsString();
                    String nextButtonTag =
                            extensionObject.get("next-button-tag").getAsString();
                    String lastButtonTag =
                            extensionObject.get("back-button-tag").getAsString();
                    String slotButtonTag =
                            extensionObject.get("slot-button-tag").getAsString();

                    int startSlot = extensionObject.get("start-slot").getAsInt();
                    int endSlot = extensionObject.get("end-slot").getAsInt();

                    methodCarrier slotClickedListener = null;
                    if (taggedMethods.containsKey(slotButtonTag)) {
                        slotClickedListener = taggedMethods.get(slotButtonTag);

                        if (slotClickedListener == null) {
                            instance.getLogger()
                                    .warning("Could not find listener for " + slotButtonTag + " in GUI Type "
                                            + this.getClass().getSimpleName());
                            continue;
                        }

                        if (slotClickedListener.method.getParameterCount() != 2
                                || (slotClickedListener.method.getParameterTypes()[0] != InventoryClickEvent.class
                                        || slotClickedListener.method.getParameterTypes()[1] != int.class))
                            slotClickedListener = null;
                    }

                    ItemStack[] items = null;
                    if (taggedItemGenerators.containsKey(genTag)) {
                        methodCarrier m = taggedItemGenerators.get(genTag);
                        if (m.method.getReturnType() != ItemStack[].class)
                            throw new RuntimeException(
                                    "Method \"" + m.method.getName() + "\" does not return ItemStack[]");

                        try {
                            items = (ItemStack[]) m.method.invoke(m.instance);

                        } catch (IllegalAccessException | InvocationTargetException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    PagedArray array = new PagedArray(this, items, startSlot, endSlot, slotClickedListener.method);
                    this.registerComponent(name, array);

                    try {
                        taggedMethods.put(
                                nextButtonTag,
                                new methodCarrier(array.getClass().getDeclaredMethod("nextPage"), array));
                        taggedMethods.put(
                                lastButtonTag,
                                new methodCarrier(array.getClass().getDeclaredMethod("previousPage"), array));
                    } catch (NoSuchMethodException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }

    private void readNonStaticExtensions() {
        if (!parentObject.has("other")) return;

        JsonArray extensionArray = parentObject.get("other").getAsJsonArray();
        for (int i = 0; i < extensionArray.size(); i++) {
            JsonObject extensionObject = extensionArray.get(i).getAsJsonObject();

            String type = extensionObject.get("type").getAsString();
            switch (type.toUpperCase()) {
                case ("FILL") -> {
                    ItemStack[] items = new ItemStack[1];
                    if (extensionObject.has("item")) {
                        String itemName = extensionObject.get("item").getAsString();
                        items[0] = getTaggedItem(itemName);
                    } else {
                        String genTag = extensionObject.get("generator-tag").getAsString();
                        if (taggedItemGenerators.containsKey(genTag)) {
                            methodCarrier m = taggedItemGenerators.get(genTag);
                            if (m.method.getReturnType() != ItemStack[].class)
                                throw new RuntimeException(
                                        "Method \"" + m.method.getName() + "\" does not return ItemStack[]");

                            try {
                                items = (ItemStack[]) m.method.invoke(m.instance);

                            } catch (IllegalAccessException | InvocationTargetException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }

                    int startSlot = extensionObject.get("start-slot").getAsInt();
                    int endSlot = extensionObject.get("end-slot").getAsInt();

                    for (int slot = startSlot; slot <= endSlot; slot++) {
                        int itemIndex = 0;
                        // if (endSlot - (startSlot - 1) == items.length) itemIndex = slot - startSlot;

                        slotsToSet.add(new slotCarrier(slot, items[itemIndex]));
                    }
                }
            }
        }
    }

    private void readItems() {

        generatedItems.clear();

        // region Item Reading
        JsonArray declaredItems = parentObject.get("items").getAsJsonArray();
        for (int i = 0; i < declaredItems.size(); i++) {
            JsonObject itemObject = declaredItems.get(i).getAsJsonObject();
            String name = itemObject.get("name").getAsString();

            if (itemObject.has("generator-tag")) {
                String tag = itemObject.get("generator-tag").getAsString();
                if (!taggedItemGenerators.containsKey(tag)) continue;

                try {
                    generatedItems.put(name, (ItemStack)
                            taggedItemGenerators.get(tag).method.invoke(taggedItemGenerators.get(tag).instance));
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            } else {
                String material = itemObject.get("type").getAsString();
                String title = material.equalsIgnoreCase("AIR")
                        ? ""
                        : getText(itemObject.get("title").getAsString());
                List<String> itemLore = new ArrayList<>();

                if (itemObject.has("lore")) {
                    JsonArray metaArray = itemObject.get("lore").getAsJsonArray();
                    for (int line = 0; line < metaArray.size(); line++) {
                        itemLore.add(getText(metaArray.get(line).getAsString()));
                    }
                }

                ItemStack item = new ItemStack(Material.valueOf(material.toUpperCase()));
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(title);
                    if (itemLore.size() > 0) meta.setLore(itemLore);

                    meta.addItemFlags(
                            ItemFlag.HIDE_ATTRIBUTES,
                            ItemFlag.HIDE_DESTROYS,
                            ItemFlag.HIDE_ENCHANTS,
                            ItemFlag.HIDE_DYE,
                            ItemFlag.HIDE_UNBREAKABLE);
                    item.setItemMeta(meta);
                }

                generatedItems.put(name, item);
            }
        }
        // endregion
    }

    private void readSlots() {
        // region Slot Reading
        JsonArray declaredSlots = parentObject.get("slots").getAsJsonArray();
        for (int i = 0; i < declaredSlots.size(); i++) {
            JsonObject slotObject = declaredSlots.get(i).getAsJsonObject();

            int slot = slotObject.get("slot").getAsInt();
            String itemName = slotObject.get("item").getAsString();

            if (slotObject.has("condition")) {
                String condition = slotObject.get("condition").getAsString();
                String[] parts = condition.split("=");
                String fieldTag = parts[0];
                if (!taggedFields.containsKey(fieldTag)) {
                    Bukkit.getLogger()
                            .warning("[LuckyGUI] could not find field with tag \"" + fieldTag + "\" for GUI " + title);
                    continue;
                }

                try {
                    fieldCarrier selectedField = taggedFields.get(fieldTag);
                    Class<?> clazz = selectedField.field.getType();
                    if (clazz == boolean.class || clazz == Boolean.class) {
                        boolean fieldValue = selectedField.field.getBoolean(selectedField.instance);

                        if (Boolean.parseBoolean(parts[1]) != fieldValue) continue;
                    } else if (clazz == int.class || clazz == Integer.class) {
                        int fieldValue = selectedField.field.getInt(selectedField.instance);

                        if (Integer.parseInt(parts[1]) != fieldValue) continue;
                    } else if (clazz == float.class || clazz == Float.class) {
                        float fieldValue = selectedField.field.getFloat(selectedField.instance);

                        if (Float.parseFloat(parts[1]) != fieldValue) continue;
                    } else if (clazz == String.class) {
                        if (selectedField.field.get(selectedField.instance) == null) continue;
                        if (!((String) selectedField.field.get(selectedField.instance)).equalsIgnoreCase(parts[1]))
                            continue;
                    }

                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }

            if (!generatedItems.containsKey(itemName)) continue;

            slotsToSet.add(new slotCarrier(slot, generatedItems.get(itemName)));

            if (slotObject.has("button-tag")) {
                String buttonTag = slotObject.get("button-tag").getAsString();
                if (!taggedMethods.containsKey(buttonTag)) {
                    Bukkit.getLogger()
                            .warning("[LuckyGUI] could not find void (buttonListener) with tag \"" + buttonTag
                                    + "\" for GUI " + title);
                    continue;
                }

                registeredListeners.put(slot, taggedMethods.get(buttonTag));
            } else if (slotObject.has("unlocked")) {
                setInteractable(slot, slotObject.get("unlocked").getAsBoolean());
            }

            if (slotObject.has("slot-tag")) {
                String slotTag = slotObject.get("slot-tag").getAsString();
                taggedSlots.put(slotTag, slot);
            }
        }
        // endregion
    }

    public void reflectObject(Object instance) throws InvocationTargetException, IllegalAccessException {
        for (Method method : instance.getClass().getDeclaredMethods()) {
            if (!method.isAnnotationPresent(GUITag.class)) continue;

            String key = method.getAnnotation(GUITag.class).value();

            if (method.getReturnType() == void.class || method.getReturnType() == String.class) {
                method.setAccessible(true);
                taggedMethods.put(key, new methodCarrier(method, instance));
            } else if (method.getReturnType() == ItemStack.class || method.getReturnType() == ItemStack[].class) {
                method.setAccessible(true);
                taggedItemGenerators.put(key, new methodCarrier(method, instance));
            }
        }

        for (Field field : instance.getClass().getDeclaredFields()) {
            if (!field.isAnnotationPresent(GUITag.class)) continue;

            String key = field.getAnnotation(GUITag.class).value();
            field.setAccessible(true);
            taggedFields.put(key, new fieldCarrier(field, instance));
        }
    }

    private String getText(String text) {
        String langID = null;
        boolean replaceParts = false;

        if (text.contains(":")) {
            String[] args = text.split(":");

            for (int i = 0; i < args.length; i++) {
                switch (args[i]) {
                    case ("LANG") -> {
                        if (i + 1 >= args.length) continue;

                        langID = args[i + 1];
                    }
                    case ("REPLACE") -> replaceParts = true;
                }
            }
        }

        if (langID != null) {
            text = langFile.getText(langID, this);
        }

        if (replaceParts) {
            for (Map.Entry<String, String> entry : textReplacements.entrySet()) {
                text = text.replace(entry.getKey(), entry.getValue());
            }
        }

        return ChatColor.translateAlternateColorCodes('&', text);
    }

    protected final void addTextReplacement(String[]... replacementPairs) {
        for (int i = 0; i < replacementPairs.length; i++) {
            textReplacements.put(replacementPairs[i][0], replacementPairs[i][1]);
        }
    }

    protected final void addTextReplacement(String key, String value) {
        textReplacements.put(key, value);
    }

    private ItemStack getTaggedItem(String tag) {
        if (!generatedItems.containsKey(tag)) {
            Bukkit.getLogger().warning("Could not find item tagged " + tag + " for GUI " + title);
            return null;
        }

        return generatedItems.get(tag);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected final void constructView() {

        slotsToSet.clear();

        readNonStaticExtensions();
        readSlots();

        // Background Fill
        if (parentObject.has("backfill")) {
            String backgroundItemName = parentObject.get("backfill").getAsString();
            for (int slot = 0; slot < inventory.getSize(); slot++) {
                setItem(slot, getTaggedItem(backgroundItemName));
            }
        }

        // Slots
        for (slotCarrier carrier : slotsToSet) {
            setItem(carrier.slot, carrier.item);
        }
    }

    /**
     * {@inheritDoc}
     * @param e The {@link InventoryClickEvent InventoryClickEvent} instance
     */
    @Override
    protected final void onSlotClicked(InventoryClickEvent e) {
        if (!e.getClickedInventory().equals(inventory)) {
            return;
        }

        if (e.getView() != view) {
            return;
        }

        if (e.getAction().equals(InventoryAction.MOVE_TO_OTHER_INVENTORY) && allowShiftClick && e.isCancelled()) {
            e.setCancelled(false);
        }

        try {
            int slot = e.getSlot();

            if (registeredListeners.containsKey(slot)) {
                methodCarrier toInvoke = registeredListeners.get(slot);

                if (toInvoke.method.getParameterCount() == 0) {
                    toInvoke.method.invoke(toInvoke.instance);
                } else {
                    toInvoke.method.invoke(toInvoke.instance, e);
                }
            }
        } catch (InvocationTargetException | IllegalAccessException ex) {
            throw new RuntimeException(ex);
        }
    }

    // region background stuff

    /**
     * {@inheritDoc}
     * @return returns the new Inventory Instance
     */
    @Override
    protected final Inventory createInventory() {

        Inventory inv;

        if (type == InventoryType.CHEST) {
            inv = Bukkit.createInventory(null, size, title);
        } else {
            inv = Bukkit.createInventory(null, type, title);
        }

        return inv;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected final void openLogic() {
        view = user.openInventory(inventory);
    }

    /**
     * {@inheritDoc}
     * @param exitCode the Exit-Code of the GUI (ignore if not working with packets)
     */
    @Override
    protected final void closeLogic(int exitCode) {
        if (user.getOpenInventory() == view) {
            user.closeInventory();
        }

        onClose();
    }

    /**
     * {@inheritDoc}
     * @param e The {@link InventoryDragEvent InventoryDragEvent} instance
     */
    @Override
    protected final void onSlotDragged(InventoryDragEvent e) {}

    /**
     * {@inheritDoc}
     * @param e The {@link InventoryCloseEvent InventoryClickEvent} instance
     */
    @Override
    protected final void onInventoryClosed(InventoryCloseEvent e) {}

    /**
     * Gets called when the GUI is closing
     */
    protected void onClose() {}
    // endregion

    /**
     * Marks fields or methods with a specified tag which is used when the File-GUI is decoding the json data
     * @author Nick Balischewski
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD, ElementType.FIELD})
    public @interface GUITag {
        String value();
    }

    /**
     * Will clear all preloaded GUI-Json Objects from the registry
     * Call when reloading Config/Lang/GUI Files
     */
    public static void clearGUIData() {
        files.clear();
    }

    /**
     * Returns the item at the slot with the "slot-tag" {tag}
     * @param tag The specified tag
     * @return The item at the tagged slot (null when tag is not present)
     */
    protected final ItemStack getItem(String tag) {
        if (!taggedSlots.containsKey(tag)) return null;

        return getItem(taggedSlots.get(tag));
    }

    private static JsonObject loadGUIFile(String guiName, Plugin instance) {

        final String fileName = guiName.endsWith(".json") ? guiName : guiName + ".json";

        return files.computeIfAbsent(fileName, o -> {
            InputStream stream = null;
            Reader reader;
            File guiFile = new File(instance.getDataFolder() + "/LuckyGUI/" + fileName);
            if (guiFile.exists()) {
                try {
                    reader = new FileReader(guiFile);
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }
            } else {
                stream = FileGUI.class.getClassLoader().getResourceAsStream("LuckyGUI/" + fileName);

                if (stream == null) {
                    throw new RuntimeException("Could not find file: \"" + fileName
                            + "\" in resources or data folder, please make sure it is located in \"plugins/"
                            + instance.getName() + "/LuckyGUI/...\" or contact the developer of this plugin");
                }

                reader = new InputStreamReader(stream);
            }

            JsonObject mainObject = JsonParser.parseReader(reader).getAsJsonObject();
            try {
                reader.close();
                if (stream != null) {
                    stream.close();
                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            return mainObject;
        });
    }

    /**
     * Saves the "default" version of a file GUI (The json file within the projects resources) to the plugins data-folder
     * @param fileName the GUI-name (with or without ".json")
     * @param instance the Main Plugin instance
     */
    public static void saveDefaultGUI(String fileName, Plugin instance) {
        if (!fileName.endsWith(".json")) fileName += ".json";

        File guiFile = new File(instance.getDataFolder() + "/LuckyGUI/" + fileName);
        if (guiFile.exists()) return;

        guiFile.getParentFile().mkdirs();
        try {
            if (!guiFile.createNewFile()) throw new RuntimeException("Failed to create file");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try (InputStream inputStream = FileGUI.class.getClassLoader().getResourceAsStream("LuckyGUI/" + fileName)) {

            if (inputStream == null) throw new RuntimeException("Could not create inputStream");

            try (FileOutputStream outputStream = new FileOutputStream(guiFile, false)) {
                int read;
                byte[] bytes = new byte[1024];
                while ((read = inputStream.read(bytes)) != -1) {
                    outputStream.write(bytes, 0, read);
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private record slotCarrier(int slot, ItemStack item) {}

    private record fieldCarrier(Field field, Object instance) {}

    private record methodCarrier(Method method, Object instance) {}
}

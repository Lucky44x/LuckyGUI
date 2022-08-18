# LuckyGUI
A simple Spigot based de.lucky44.gui.GUI-library

__Disclaimer: This isn't a finished project, and currently the library has some unecessery features__

To use, you'll have to create a new de.lucky44.gui.GUIManager in your onEnable method

You can create new classes which implement the de.lucky44.gui.GUI class, leaving you to override three methods:
```
@Override
public void onOpen(Player user)
```
- Gets called when the de.lucky44.gui.GUI is openend for a player
```
@Override
public void onClose()
```
- Gets called when the de.lucky44.gui.GUI is closed  

```
@Override
public void onClick(int slot)
```
- Gets called when the player clicks an item in the de.lucky44.gui.GUI


On top of that, you can also __style__ the de.lucky44.gui.GUI with three functions (you should call them in your constructor in the following order):
```
setSize(int size)
```
- Sets the inventory size (only multiples of  will work)

```
setName(String name)
```
- Sets the name of the de.lucky44.gui.GUI

```
construct()
```
- Has to be called to apply changes to the name and the size of the de.lucky44.gui.GUI

```
fill(ItemStack item)
```
- Fills the entire de.lucky44.gui.GUI with the given Item

```
set(ItemStack item, int slot)
```
- Sets the item at the given slot to the given item


A complete de.lucky44.gui.GUI object might look something like this:
```
class myNewGUI implements de.lucky44.gui.GUI{

  public myNewGUI(){
    constructView(null);
  }
  
  private void constructView(Player p){
    setSize(9);
    setName(p == null ? "TMP" : p.getName());
    construct();
    fill(backgroundItem);
    set(ConfirmButton, 0);
    set(CancelButton, 8);
  }

  @Override
  public void onOpen(Player p){
    constructView(p);
  }
  
  @Override
  public void onClose(){}
  
  @Override
  public void onClick(int slot){
    switch(slot){
      case(0):
        confirm();
        break;
        
      case(9):
        cancel();
        break;
    }
  }
}
```

Before opening this de.lucky44.gui.GUI however, you'll have to create a de.lucky44.gui.GUIManager, with a plugin instance
```
de.lucky44.gui.GUIManager manager = new de.lucky44.gui.GUIManager(this);
```
Since this de.lucky44.gui.GUIManager is written in a Singleton pattern, it's enough to create it once in the onEnable method of your plugin

When you want to open a de.lucky44.gui.GUI object for a player, you'll have to create a new objetc of this de.lucky44.gui.GUI type first:
```
myNewGUI toOpen = new myNewGUI();
```

Then you can open this **specific** de.lucky44.gui.GUI object for the player, keep in mind, that you should only open one specific object for each player
```
toOpen.open(player);
```

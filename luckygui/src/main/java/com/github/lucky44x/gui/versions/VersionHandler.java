package com.github.lucky44x.gui.versions;


import com.github.lucky44x.gui.abstraction.VersionWrapper;
import org.bukkit.Bukkit;

/**
 * Get the Servers NMS Version and returns the according version Wrapper
 * - Code is basically copied from Wesley Smith's <a href="https://github.com/WesJD/AnvilGUI/blob/master/api/src/main/java/net/wesjd/anvilgui/version/VersionMatcher.java">Anvil-GUI</a>
 *
 * @author Nick Balischewski
 */
public class VersionHandler {

    /**
     * Gets the appropriate Wrapper for the Server's NMS version
     * @return The Wrapper for the Server's NMS version
     */
    public VersionWrapper getWrapper() {
        final String serverVersion = Bukkit.getServer()
                .getClass()
                .getPackage()
                .getName()
                .split("\\.")[3]
                .substring(1);
        try {
            return (VersionWrapper) Class.forName(getClass().getPackage().getName() + ".Wrapper" + serverVersion)
                    .getDeclaredConstructor()
                    .newInstance();
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("LuckyGUI does not support version \"" + serverVersion + "\" \n" + e);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(
                    "Failed to instantiate Wrapper for version \"" + serverVersion + "\" \n" + e);
        }
    }
}

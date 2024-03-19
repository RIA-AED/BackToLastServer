package ink.magma.backtolastserver.storage;

import ink.magma.backtolastserver.platform.PlatformHandler;
import ink.magma.backtolastserver.platform.bungee.BackToLastServerBungee;
import ink.magma.backtolastserver.platform.velocity.BackToLastServerVelocity;

import java.io.File;

public class PluginFolderHandler {
    static File getPluginFolder() {
        if (PlatformHandler.getPlatformType().equals(PlatformHandler.PlatformType.Velocity)) {
            return BackToLastServerVelocity.dataDirectory.toFile();
        } else if (PlatformHandler.getPlatformType().equals(PlatformHandler.PlatformType.Bungee)) {
            return BackToLastServerBungee.instance.getDataFolder();
        }
        return null;
    }

    static File getFileInPluginFolder(String child) {
        return new File(getPluginFolder(), child);
    }
}

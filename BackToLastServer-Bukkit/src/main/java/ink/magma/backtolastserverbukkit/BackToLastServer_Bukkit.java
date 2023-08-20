package ink.magma.backtolastserverbukkit;

import ink.magma.backtolastserverbukkit.Listener.LoginListener;
import org.bukkit.plugin.java.JavaPlugin;

public final class BackToLastServer_Bukkit extends JavaPlugin {

    public static JavaPlugin instance;

    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;
        getServer().getMessenger().registerOutgoingPluginChannel(this, "back-to-last-server:authme-login");
        getServer().getPluginManager().registerEvents(new LoginListener(), this);
        getLogger().info("Loaded!");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getServer().getMessenger().unregisterIncomingPluginChannel(this, "back-to-last-server:authme-login");
        getLogger().info("Unloaded!");
    }

}

package ink.magma.backtolastserver.platform.bungee;

import ink.magma.backtolastserver.platform.PlatformHandler;
import ink.magma.backtolastserver.platform.bungee.command.DisableCommand;
import ink.magma.backtolastserver.platform.bungee.listener.PlayerListener;
import ink.magma.backtolastserver.storage.StorageContainer;
import net.kyori.adventure.platform.bungeecord.BungeeAudiences;
import net.md_5.bungee.api.plugin.Plugin;
import org.checkerframework.checker.nullness.qual.NonNull;
import revxrsal.commands.bungee.BungeeCommandHandler;

public class BackToLastServerBungee extends Plugin {
    public StorageContainer storageContainer;
    public static BackToLastServerBungee instance;
    private BungeeAudiences adventure;


    @Override
    public void onEnable() {
        instance = this;
        PlatformHandler.setPlatformType(PlatformHandler.PlatformType.Bungee);
        this.adventure = BungeeAudiences.create(this);
        getDataFolder().mkdirs();
        storageContainer = new StorageContainer();

        BungeeCommandHandler commandHandler = BungeeCommandHandler.create(this);
        commandHandler.register(new DisableCommand());

        getProxy().getPluginManager().registerListener(this, new PlayerListener());
    }

    @Override
    public void onDisable() {
        if (this.adventure != null) {
            this.adventure.close();
            this.adventure = null;
        }
    }

    public @NonNull BungeeAudiences adventure() {
        if (this.adventure == null) {
            throw new IllegalStateException("Cannot retrieve audience provider while plugin is not enabled");
        }
        return this.adventure;
    }

}

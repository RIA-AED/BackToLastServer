package ink.magma.backtolastserver.platform.velocity;

import com.google.common.io.ByteArrayDataInput;
import com.google.inject.Inject;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import ink.magma.backtolastserver.BuildConstants;
import ink.magma.backtolastserver.platform.PlatformHandler;
import ink.magma.backtolastserver.platform.velocity.action.SendLastServer;
import ink.magma.backtolastserver.platform.velocity.command.DisableCommand;
import ink.magma.backtolastserver.platform.velocity.command.LastServerCommand;
import ink.magma.backtolastserver.storage.StorageContainer;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.time.Duration;
import java.util.Optional;

@Plugin(
        id = "backtolastserver",
        name = "BackToLastServer",
        version = BuildConstants.VERSION,
        description = "A plugin for Velocity that help players go to their last server after login",
        authors = {"MagmaBlock"}
)
public final class BackToLastServerVelocity {

    public static BackToLastServerVelocity instance;
    public static ProxyServer server;
    public static Logger logger;
    public static Path dataDirectory;

    public StorageContainer storageContainer;

    @Inject
    public BackToLastServerVelocity(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        instance = this;
        BackToLastServerVelocity.server = server;
        BackToLastServerVelocity.logger = logger;
        BackToLastServerVelocity.dataDirectory = dataDirectory;

        PlatformHandler.setPlatformType(PlatformHandler.PlatformType.Velocity);

        dataDirectory.toFile().mkdirs();
        storageContainer = new StorageContainer();
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        CommandManager commandManager = server.getCommandManager();

        BrigadierCommand lastServerCommand = LastServerCommand.createBrigadierCommand(server);
        DisableCommand disableCommand = new DisableCommand();

        commandManager.register("lastserver", lastServerCommand);
        commandManager.register("togglelastserver", disableCommand);

        logger.info("BackToLastServer has loaded!");
    }

    @Subscribe
    public void onPlayerDisconnent(DisconnectEvent event) {
        String playerUUID = event.getPlayer().getUniqueId().toString();

        Optional<ServerConnection> serverConnection = event.getPlayer().getCurrentServer();
        if (serverConnection.isPresent()) {
            String serverID = serverConnection.get().getServerInfo().getName();

            this.storageContainer.lastServerStore.setHistory(playerUUID, serverID);
            this.storageContainer.lastServerStore.saveAllHistory();
        }
    }

    @Subscribe
    public void onBukkitAuthmeLogin(PluginMessageEvent event) {
        if (event.getIdentifier().getId().equals("authmevelocity:main")) {
            final ByteArrayDataInput input = event.dataAsDataStream();
            final String message = input.readUTF();
            final String name = input.readUTF();
            final Player player = server.getPlayer(name).orElse(null);

            if (message.equals("LOGIN") && player != null) {
                // 避免关闭的玩家被传送
                if (!this.storageContainer.disableStore.getIsEnable(player.getUniqueId().toString())) return;

                server.getScheduler().
                        buildTask(this, () -> {
                            SendLastServer.sendPlayerLastServer(player);
                            this.storageContainer.lastServerStore.removeHistory(player.getUniqueId().toString()); // 避免二次触发
                            this.storageContainer.lastServerStore.saveAllHistory();
                        })
                        .delay(Duration.ofSeconds(1))
                        .schedule();
            }
        }
    }
}

package ink.magma.backtolastserver;

import com.google.inject.Inject;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import ink.magma.backtolastserver.command.LastServerCommand;
import ink.magma.backtolastserver.store.LastServerStore;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.Optional;

@Plugin(
        id = "backtolastserver",
        name = "BackToLastServer",
        version = BuildConstants.VERSION,
        description = "A plugin for Velocity that help players go to their last server after login",
        authors = {"MagmaBlock"}
)
public final class BackToLastServer {

    public static ProxyServer server;
    public static Logger logger;
    public static Path dataDirectory;

    public static LastServerStore lastServerStore;

    @Inject
    public BackToLastServer(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        BackToLastServer.server = server;
        BackToLastServer.logger = logger;
        BackToLastServer.dataDirectory = dataDirectory;

        dataDirectory.toFile().mkdirs();

        lastServerStore = new LastServerStore();
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        CommandManager commandManager = server.getCommandManager();
        CommandMeta commandMeta = commandManager.metaBuilder("backtolastserver")
                .plugin(this)
                .build();
        BrigadierCommand lastServerCommand = LastServerCommand.createBrigadierCommand(server);
        commandManager.register(commandMeta, lastServerCommand);

        logger.info("BackToLastServer has loaded!");
    }

    @Subscribe
    public void onPlayerDisconnent(DisconnectEvent event) {
        String playerUUID = event.getPlayer().getUniqueId().toString();

        Optional<ServerConnection> serverConnection = event.getPlayer().getCurrentServer();
        if (serverConnection.isPresent()) {
            String serverID = serverConnection.get().getServerInfo().getName();

            lastServerStore.setHistory(playerUUID, serverID);
            lastServerStore.saveAllHistory();
        }
    }

    @Subscribe
    public void onPlayerAuthmeLogin() {

    }

//    @Subscribe
//    public void onPlayerLogin(ServerConnectedEvent event) {
//        // TODO
//    }

}

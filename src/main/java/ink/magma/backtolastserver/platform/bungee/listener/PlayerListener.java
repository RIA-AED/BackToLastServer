package ink.magma.backtolastserver.platform.bungee.listener;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import ink.magma.backtolastserver.platform.bungee.BackToLastServerBungee;
import ink.magma.backtolastserver.platform.bungee.action.SendLastServer;
import ink.magma.backtolastserver.storage.StorageContainer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.concurrent.TimeUnit;

public class PlayerListener implements Listener {
    @EventHandler
    public void onPlayerDisconnent(PlayerDisconnectEvent event) {
        String serverID = event.getPlayer().getServer().getInfo().getName();
        String playerUUID = event.getPlayer().getUniqueId().toString();

        if (serverID != null) {
            getStorageContainer().lastServerStore.setHistory(playerUUID, serverID);
            getStorageContainer().lastServerStore.saveAllHistory();
        }
    }


    /**
     * Functions "onAuthmePluginMessage" and "handleOnLogin" source came from:
     * <a href="https://github.com/AuthMe/AuthMeBungee/blob/master/src/main/java/fr/xephi/authmebungee/listeners/BungeeMessageListener.java">AuthmeBungee</a>
     */
    @EventHandler
    public void onAuthmePluginMessage(final PluginMessageEvent event) {
        if (event.isCancelled()) return;

        // Check if the message is for a server (ignore client messages)
        if (!event.getTag().equals("BungeeCord")) return;

        // Check if a player is not trying to send us a fake message
        if (!(event.getSender() instanceof Server)) return;

        // Read the plugin message
        final ByteArrayDataInput in = ByteStreams.newDataInput(event.getData());

        // Accept only broadcasts
        if (!in.readUTF().equals("Forward")) return;

        in.readUTF(); // Skip ONLINE/ALL parameter

        // Let's check the subchannel
        if (!in.readUTF().equals("AuthMe.v2.Broadcast")) return;

        // Read data byte array
        final short dataLength = in.readShort();
        final byte[] dataBytes = new byte[dataLength];
        in.readFully(dataBytes);
        final ByteArrayDataInput dataIn = ByteStreams.newDataInput(dataBytes);

        // For now that's the only type of message the server is able to receive
        final String type = dataIn.readUTF();
        if (type.equals("login")) {
            handleOnLogin(dataIn);
        }
    }

    private void handleOnLogin(final ByteArrayDataInput in) {
        final String name = in.readUTF();
        ProxiedPlayer player = BackToLastServerBungee.instance.getProxy().getPlayer(name);

        // 避免关闭的玩家被传送
        if (!BackToLastServerBungee.instance.storageContainer.disableStore.getIsEnable(player.getUniqueId().toString()))
            return;

        BackToLastServerBungee.instance.getProxy().getScheduler().schedule(
                BackToLastServerBungee.instance,
                () -> {
                    SendLastServer.sendPlayerLastServer(player);
                    BackToLastServerBungee.instance.storageContainer.lastServerStore.removeHistory(player.getUniqueId().toString()); // 避免二次触发
                    BackToLastServerBungee.instance.storageContainer.lastServerStore.saveAllHistory();
                },
                500,
                TimeUnit.MILLISECONDS
        );
    }

    private static StorageContainer getStorageContainer() {
        return BackToLastServerBungee.instance.storageContainer;
    }
}

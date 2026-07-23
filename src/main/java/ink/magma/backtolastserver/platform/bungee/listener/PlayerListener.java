package ink.magma.backtolastserver.platform.bungee.listener;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;

import ink.magma.backtolastserver.logger.UniLogger;
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
        ProxiedPlayer player = event.getPlayer();
        Server server = player.getServer();
        String playerUUID = player.getUniqueId().toString();

        if (server == null) {
            UniLogger.warn(
                    "PlayerDisconnectEvent: " + player.getName() +
                            ", UUID=" + playerUUID + ", getServer() 为 null! 跳过历史记录保存。这可能是因为玩家意外离线，可安全忽略。");
            return;
        }

        String serverID = server.getInfo().getName();
        if (serverID != null) {
            getStorageContainer().lastServerStore.setHistory(playerUUID, serverID);
            getStorageContainer().lastServerStore.requestSave();
        }
    }

    /**
     * Functions "onAuthmePluginMessage" and "handleOnLogin" source came from:
     * <a href=
     * "https://github.com/AuthMe/AuthMeBungee/blob/master/src/main/java/fr/xephi/authmebungee/listeners/BungeeMessageListener.java">AuthmeBungee</a>
     * <p>
     * 兼容 AuthMe 5.x 的旧协议 (BungeeCord/Forward/AuthMe.v2.Broadcast)。
     */
    @EventHandler
    public void onAuthmePluginMessage(final PluginMessageEvent event) {
        if (event.isCancelled())
            return;

        // Check if the message is for a server (ignore client messages)
        if (!event.getTag().equals("BungeeCord"))
            return;

        // Check if a player is not trying to send us a fake message
        if (!(event.getSender() instanceof Server))
            return;

        // Read the plugin message
        final ByteArrayDataInput in = ByteStreams.newDataInput(event.getData());

        // Accept only broadcasts
        if (!in.readUTF().equals("Forward"))
            return;

        in.readUTF(); // Skip ONLINE/ALL parameter

        // Let's check the subchannel
        if (!in.readUTF().equals("AuthMe.v2.Broadcast"))
            return;

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

    /**
     * 兼容 AuthMe 6.0.0+ 的新协议: 自定义频道 {@code authme:main}，
     * payload 为 {@code MessageType.getId()} (login 动作为 "login") + 小写用户名。
     * 参考上游 {@code fr.xephi.authme.bungee.BungeeProxyBridge} 的接收逻辑。
     */
    @EventHandler
    public void onAuthmePluginMessageV6(final PluginMessageEvent event) {
        if (event.isCancelled())
            return;

        // AuthMe 6.0.0+ 使用自定义频道 authme:main
        if (!event.getTag().equals("authme:main"))
            return;

        // Check if a player is not trying to send us a fake message
        if (!(event.getSender() instanceof Server))
            return;

        try {
            // Read the plugin message
            final ByteArrayDataInput in = ByteStreams.newDataInput(event.getData());

            // 只处理 login 动作, 其余类型 (logout / perform.login.ack / premium.*) 忽略
            final String type = in.readUTF();
            if (type.equals("login")) {
                handleOnLogin(in);
            }
        } catch (IllegalStateException e) {
            UniLogger.warn("收到格式错误的 AuthMe authme:main 插件消息，已忽略。");
        }
    }

    private void handleOnLogin(final ByteArrayDataInput in) {
        final String name = in.readUTF();
        ProxiedPlayer player = BackToLastServerBungee.instance.getProxy().getPlayer(name);
        if (player == null) {
            UniLogger.warn("AuthMe login 触发: 未找到玩家 '" + name + "', 跳过回到上次子服。");
            return;
        }

        UniLogger.info("AuthMe login 触发: 玩家 " + player.getName() + " 认证成功, 准备回到上次子服。");

        // 避免关闭的玩家被传送
        if (!BackToLastServerBungee.instance.storageContainer.disableStore.getIsEnable(player.getUniqueId().toString()))
            return;

        BackToLastServerBungee.instance.getProxy().getScheduler().schedule(
                BackToLastServerBungee.instance,
                () -> {
                    SendLastServer.sendPlayerLastServer(player);
                    BackToLastServerBungee.instance.storageContainer.lastServerStore
                            .removeHistory(player.getUniqueId().toString()); // 避免二次触发
                    BackToLastServerBungee.instance.storageContainer.lastServerStore.requestSave();
                },
                500,
                TimeUnit.MILLISECONDS);
    }

    private static StorageContainer getStorageContainer() {
        return BackToLastServerBungee.instance.storageContainer;
    }
}

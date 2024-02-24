package ink.magma.backtolastserver.platform.velocity.action;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import ink.magma.backtolastserver.message.MessageManager;
import ink.magma.backtolastserver.platform.velocity.BackToLastServerVelocity;
import ink.magma.backtolastserver.storage.DisableServerStore;
import ink.magma.backtolastserver.storage.LastServerStore;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import us.ajg0702.queue.api.AjQueueAPI;
import us.ajg0702.queue.api.server.AdaptedServer;

import java.util.Optional;

public class SendLastServer {
    /**
     * Send player to their last server.
     * If no history / server not found, return 0
     *
     * @param onlinePlayer player to send
     * @return 0 or 1
     */
    static public boolean sendPlayerLastServer(@NotNull Player onlinePlayer) {
        // 查看历史记录文件中是否存在此玩家的记录
        String lastServerID = LastServerStore.serverHistory.get(onlinePlayer.getUniqueId().toString());
        if (lastServerID == null) return false;

        // 查看目标服务器 ID 是否在黑名单中
        if (DisableServerStore.disableServerList.contains(lastServerID)) return false;

        // 查看目标服务器是否存在
        Optional<RegisteredServer> targetServer = BackToLastServerVelocity.server.getServer(lastServerID);
        if (targetServer.isEmpty()) {
            onlinePlayer.sendMessage(MessageManager.lastServerNotFound());
            return false;
        }

        // 如果玩家当前已在目标服务器则什么也不做
        Optional<ServerConnection> currentServer = onlinePlayer.getCurrentServer();
        if (currentServer.isPresent()) {
            boolean equals = currentServer.get().getServerInfo().getName().equals(targetServer.get().getServerInfo().getName());
            if (equals) return true;
        }

        // 给玩家发送消息
        Component msg = MessageManager.sendPlayerToLastServer(lastServerID);
        onlinePlayer.sendMessage(msg);

        // ajQueue 将玩家加入队列
        AdaptedServer ajQueueServer = AjQueueAPI.getInstance().getPlatformMethods().getServer(lastServerID);
        AjQueueAPI.getInstance().getPlatformMethods().getPlayer(onlinePlayer.getUniqueId()).connect(ajQueueServer);

        return true;
    }
}

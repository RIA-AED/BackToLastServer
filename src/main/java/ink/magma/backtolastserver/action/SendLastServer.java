package ink.magma.backtolastserver.action;

import com.velocitypowered.api.proxy.ConnectionRequestBuilder;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import ink.magma.backtolastserver.BackToLastServer;
import ink.magma.backtolastserver.store.LastServerStore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;

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

        // 查看目标服务器是否存在
        Optional<RegisteredServer> targetServer = BackToLastServer.server.getServer(lastServerID);
        if (targetServer.isEmpty()) {
            onlinePlayer.sendMessage(Component.text("找不到上次所在的服务器.").color(NamedTextColor.GRAY));
            return false;
        }

        // 如果玩家当前已在目标服务器则什么也不做
        Optional<ServerConnection> currentServer = onlinePlayer.getCurrentServer();
        if (currentServer.isPresent()) {
            boolean equals = currentServer.get().getServerInfo().getName().equals(targetServer.get().getServerInfo().getName());
            if (equals) return true;
        }

        // 给玩家发送消息
        Component msg = Component.text("正在尝试将您送回上一次的服务器 (").color(NamedTextColor.GRAY)
                .append(Component.text(lastServerID).color(NamedTextColor.WHITE))
                .append(Component.text(").").color(NamedTextColor.GRAY));
        onlinePlayer.sendMessage(msg);

        // 声明连接
        ConnectionRequestBuilder requestBuilder = onlinePlayer.createConnectionRequest(targetServer.get());
        // 建立连接 (可能失败)
        requestBuilder.connect();
        return true;
    }
}

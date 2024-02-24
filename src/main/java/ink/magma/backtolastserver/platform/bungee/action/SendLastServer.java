package ink.magma.backtolastserver.platform.bungee.action;


import ink.magma.backtolastserver.message.MessageManager;
import ink.magma.backtolastserver.platform.bungee.BackToLastServerBungee;
import ink.magma.backtolastserver.storage.DisableServerStore;
import ink.magma.backtolastserver.storage.LastServerStore;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.jetbrains.annotations.NotNull;
import us.ajg0702.queue.api.AjQueueAPI;
import us.ajg0702.queue.api.players.AdaptedPlayer;

public class SendLastServer {
    /**
     * Send player to their last server.
     * If no history / server not found, return 0
     *
     * @param onlinePlayer player to send
     * @return 0 or 1
     */
    static public boolean sendPlayerLastServer(@NotNull ProxiedPlayer onlinePlayer) {
        // 查看历史记录文件中是否存在此玩家的记录
        String lastServerID = LastServerStore.serverHistory.get(onlinePlayer.getUniqueId().toString());
        if (lastServerID == null) return false;

        // 查看目标服务器 ID 是否在黑名单中
        if (DisableServerStore.disableServerList.contains(lastServerID)) return false;

        // adv 听众
        Audience playerAudience = BackToLastServerBungee.instance.adventure().player(onlinePlayer);

        // 查看目标服务器是否存在
        ServerInfo serverInfo = BackToLastServerBungee.instance.getProxy().getServerInfo(lastServerID);
        if (serverInfo == null) {
            playerAudience.sendMessage(MessageManager.lastServerNotFound());
            return false;
        }

        // 如果玩家当前已在目标服务器则什么也不做
        if (onlinePlayer.getServer().getInfo().getName().equals(lastServerID)) {
            return true;
        }

        // ajQueue 将玩家加入队列
        AdaptedPlayer adaptedPlayer = AjQueueAPI.getInstance().getPlatformMethods().getPlayer(onlinePlayer.getUniqueId());
        AjQueueAPI.getInstance().getQueueManager().addToQueue(adaptedPlayer, lastServerID);

        // 给玩家发送消息
        Component msg = MessageManager.sendPlayerToLastServer(lastServerID);
        playerAudience.sendMessage(msg);

        return true;
    }
}

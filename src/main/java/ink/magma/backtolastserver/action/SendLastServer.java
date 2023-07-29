package ink.magma.backtolastserver.action;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import ink.magma.backtolastserver.BackToLastServer;
import ink.magma.backtolastserver.store.LastServerStore;
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
        String lastServerID = LastServerStore.serverHistory.get(onlinePlayer.getUniqueId().toString());

        if (lastServerID != null) {
            Optional<RegisteredServer> targetServer = BackToLastServer.server.getServer(lastServerID);
            if (targetServer.isPresent()) {
                // 得到玩家当前的子服, 与目标比对, 如果已经是则什么也不做
                Optional<ServerConnection> currentServer = onlinePlayer.getCurrentServer();
                if (currentServer.isPresent()) {
                    boolean equals = currentServer.get().getServerInfo().getName().equals(targetServer.get().getServerInfo().getName());
                    if (equals) return true;
                }

                onlinePlayer.createConnectionRequest(targetServer.get());
                return true;
            } else return false;
        } else {
            return false;
        }
    }
}

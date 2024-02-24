package ink.magma.backtolastserver.platform.velocity.command;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import ink.magma.backtolastserver.message.MessageManager;
import ink.magma.backtolastserver.platform.velocity.BackToLastServerVelocity;
import ink.magma.backtolastserver.storage.StorageContainer;
import net.kyori.adventure.text.Component;

import java.util.Optional;

public class DisableCommand implements SimpleCommand {
    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        // Get the arguments after the command alias
        String[] args = invocation.arguments();

        // 取得操作对象 UUID
        String UUIDOfPlayer;
        if (args.length != 0) {
            Optional<Player> targetPlayer = BackToLastServerVelocity.server.getPlayer(args[0]);
            if (targetPlayer.isEmpty()) {
                source.sendMessage(MessageManager.targetPlayerNotFound());
                return;
            }
            UUIDOfPlayer = targetPlayer.get().getUniqueId().toString();
        } else if (source instanceof Player player) {
            UUIDOfPlayer = player.getUniqueId().toString();
        } else {
            source.sendMessage(MessageManager.targetPlayerRequired());
            return;
        }

        // 为目标玩家开启或关闭
        Boolean isAllow = getStorageContainer().disableStore.getIsEnable(UUIDOfPlayer);
        getStorageContainer().disableStore.setDisable(UUIDOfPlayer, isAllow);
        getStorageContainer().disableStore.saveAllDisable();

        // 发送结果
        Component msg;
        if (isAllow) msg = MessageManager.autoFunctionDisabled();
        else msg = MessageManager.autoFunctionEnabled();
        source.sendMessage(msg);
    }

    private static StorageContainer getStorageContainer() {
        return BackToLastServerVelocity.instance.storageContainer;
    }
}

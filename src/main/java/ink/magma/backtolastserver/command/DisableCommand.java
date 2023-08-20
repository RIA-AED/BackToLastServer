package ink.magma.backtolastserver.command;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import ink.magma.backtolastserver.BackToLastServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

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
            Optional<Player> targetPlayer = BackToLastServer.server.getPlayer(args[0]);
            if (targetPlayer.isEmpty()) {
                source.sendMessage(Component.text("目标玩家不存在."));
                return;
            }
            UUIDOfPlayer = targetPlayer.get().getUniqueId().toString();
        } else if (source instanceof Player player) {
            UUIDOfPlayer = player.getUniqueId().toString();
        } else {
            source.sendMessage(Component.text("必须指定玩家"));
            return;
        }

        // 反转
        Boolean isAllow = BackToLastServer.disableStore.getIsEnable(UUIDOfPlayer);
        BackToLastServer.disableStore.setDisable(UUIDOfPlayer, isAllow);
        BackToLastServer.disableStore.saveAllDisable();

        Component msg;
        if (isAllow) msg = Component.text("已禁用.").color(NamedTextColor.RED)
                .append(Component.text(" 下次登录时, 将始终留在云端.").color(NamedTextColor.WHITE));
        else msg = Component.text("已启用.").color(NamedTextColor.GREEN)
                .append(Component.text(" 下次登录时, 将前往您上次离开的服务器.").color(NamedTextColor.WHITE));

        source.sendMessage(msg);
    }
}

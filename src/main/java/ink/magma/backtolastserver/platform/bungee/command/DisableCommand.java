package ink.magma.backtolastserver.platform.bungee.command;

import ink.magma.backtolastserver.message.MessageManager;
import ink.magma.backtolastserver.platform.bungee.BackToLastServerBungee;
import ink.magma.backtolastserver.storage.StorageContainer;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Default;

import java.util.UUID;

public class DisableCommand {
    @Command("togglelastserver")
    public void run(CommandSender sender, @Default("me") ProxiedPlayer target) {
        UUID targetUniqueId = target.getUniqueId();


        // 为目标玩家开启或关闭
        Boolean isAllow = getStorageContainer().disableStore.getIsEnable(targetUniqueId.toString());
        getStorageContainer().disableStore.setDisable(targetUniqueId.toString(), isAllow);
        getStorageContainer().disableStore.saveAllDisable();

        // 发送结果
        Component msg;
        if (isAllow) msg = MessageManager.autoFunctionDisabled();
        else msg = MessageManager.autoFunctionEnabled();
        BackToLastServerBungee.instance.adventure().sender(sender).sendMessage(msg);
    }

    private static StorageContainer getStorageContainer() {
        return BackToLastServerBungee.instance.storageContainer;
    }
}

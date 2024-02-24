package ink.magma.backtolastserver.message;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.text.MessageFormat;

public class MessageManager {
    private static MiniMessage mini = MiniMessage.miniMessage();

    public static Component sendPlayerToLastServer(String lastServerId) {
        return mini.deserialize(MessageFormat.format(
                "<gray>正在尝试将您送回上一次的服务器 (<white>{0}</white><gray>).</gray>  <gray><click:run_command:''/togglelastserver''><hover:show_text:''您可以点击此处或使用指令 <yellow>/togglelastserver</yellow><br>如果您想要重新打开, 也可在个人菜单中查看.''>[禁用]</hover></click></gray> <gray><click:run_command:''/lobby''><hover:show_text:''点击返回<br><gray>/lobby''>[返回云端]",
                lastServerId
        ));
    }

    public static Component lastServerNotFound() {
        return Component.text("找不到上次所在的服务器.").color(NamedTextColor.GRAY);
    }

    public static Component targetPlayerNotFound() {
        return Component.text("目标玩家不存在.").color(NamedTextColor.GRAY);
    }

    public static Component targetPlayerRequired() {
        return Component.text("必须指定玩家").color(NamedTextColor.GRAY);
    }

    public static Component autoFunctionDisabled() {
        return Component.text("已禁用.").color(TextColor.color(0xcf1322))
                .append(Component.text(" 下次登录时, 将始终留在云端.").color(NamedTextColor.WHITE));
    }

    public static Component autoFunctionEnabled() {
        return Component.text("已启用.").color(TextColor.color(0x73d13d))
                .append(Component.text(" 下次登录时, 将前往您上次离开的服务器.").color(NamedTextColor.WHITE));
    }

}

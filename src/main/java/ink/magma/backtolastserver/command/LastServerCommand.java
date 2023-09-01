package ink.magma.backtolastserver.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.ConsoleCommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import ink.magma.backtolastserver.action.SendLastServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.Optional;

public class LastServerCommand {
    public static BrigadierCommand createBrigadierCommand(final ProxyServer proxyServer) {
        LiteralCommandNode<CommandSource> backtolastserver = LiteralArgumentBuilder
                .<CommandSource>literal("lastserver")
                .executes(context -> {
                    if (!context.getSource().hasPermission("backtolastserver.use")) {
                        context.getSource().sendMessage(Component.text("没有使用权限."));
                        return 0;
                    }
                    if (context.getSource() instanceof ConsoleCommandSource) {
                        context.getSource().sendMessage(Component.text("你必须输入一个玩家的名字!").color(NamedTextColor.RED));
                        return 0;
                    }

                    // 获取玩家和他上一次的服务器
                    Player player = (Player) context.getSource();

                    // 尝试将玩家发送
                    SendLastServer.sendPlayerLastServer(player);

                    return 1;
                })
                .then(RequiredArgumentBuilder.<CommandSource, String>argument("target", StringArgumentType.string())
                        .suggests((ctx, builder) -> {
                            proxyServer.getAllPlayers().forEach(player -> builder.suggest(
                                    player.getUsername()
                            ));
                            return builder.buildFuture();
                        })
                        .executes(context -> {
                            if (!context.getSource().hasPermission("backtolastserver.other")) {
                                context.getSource().sendMessage(Component.text("没有使用权限."));
                                return 0;
                            }

                            String arg = context.getArgument("target", String.class);
                            Optional<Player> targetPlayer = proxyServer.getPlayer(arg);

                            if (targetPlayer.isPresent()) {
                                boolean tryResult = SendLastServer.sendPlayerLastServer(targetPlayer.get());
                                if (tryResult) {
                                    context.getSource().sendMessage(Component.text("成功. 正在发送玩家..."));
                                } else {
                                    context.getSource().sendMessage(Component.text("不存在历史或服务器查找失败."));
                                }
                            } else {
                                context.getSource().sendMessage(Component.text("玩家 " + arg + " 不存在."));
                            }
                            return 1;
                        })
                )
                .build();

        return new BrigadierCommand(backtolastserver);
    }
}
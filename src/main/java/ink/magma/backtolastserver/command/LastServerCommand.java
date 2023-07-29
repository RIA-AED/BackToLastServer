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
                .<CommandSource>literal("backtolastserver")
                .executes(context -> {
                    if (!context.getSource().hasPermission("backtolastserver.use")) {
                        context.getSource().sendMessage(Component.text("No permisson."));
                        return 0;
                    }
                    if (context.getSource() instanceof ConsoleCommandSource) {
                        context.getSource().sendMessage(Component.text("You must enter a player name!").color(NamedTextColor.RED));
                        return 0;
                    }

                    // 获取玩家和他上一次的服务器
                    Player player = (Player) context.getSource();

                    // 尝试将玩家发送
                    boolean tryResult = SendLastServer.sendPlayerLastServer(player);
                    if (tryResult) {
                        player.sendMessage(Component.text("尝试将您传送回上次所在的服务器...").color(NamedTextColor.GRAY));
                    } else {
                        player.sendMessage(Component.text("找不到上次所在的服务器.").color(NamedTextColor.GRAY));
                    }

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
                                context.getSource().sendMessage(Component.text("No permisson."));
                                return 0;
                            }

                            String arg = context.getArgument("target", String.class);
                            Optional<Player> targetPlayer = proxyServer.getPlayer(arg);

                            if (targetPlayer.isPresent()) {
                                boolean tryResult = SendLastServer.sendPlayerLastServer(targetPlayer.get());
                                if (tryResult) {
                                    context.getSource().sendMessage(Component.text("Success. Sending player..."));
                                } else {
                                    context.getSource().sendMessage(Component.text("This player has no history / The server no longer exists."));
                                }
                            } else {
                                context.getSource().sendMessage(Component.text("Player " + arg + " not find."));
                            }
                            return 1;
                        })
                )
                .build();

        return new BrigadierCommand(backtolastserver);
    }
}
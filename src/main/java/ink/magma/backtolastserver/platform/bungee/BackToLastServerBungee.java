package ink.magma.backtolastserver.platform.bungee;

import ink.magma.backtolastserver.platform.PlatformHandler;
import ink.magma.backtolastserver.platform.bungee.command.DisableCommand;
import ink.magma.backtolastserver.platform.bungee.listener.PlayerListener;
import ink.magma.backtolastserver.storage.StorageContainer;
import net.kyori.adventure.platform.bungeecord.BungeeAudiences;
import net.md_5.bungee.api.plugin.Plugin;
import org.checkerframework.checker.nullness.qual.NonNull;
import revxrsal.commands.bungee.BungeeCommandHandler;

public class BackToLastServerBungee extends Plugin {
    public StorageContainer storageContainer;
    public static BackToLastServerBungee instance;
    private BungeeAudiences adventure;


    @Override
    public void onEnable() {
        instance = this;
        PlatformHandler.setPlatformType(PlatformHandler.PlatformType.Bungee);
        this.adventure = BungeeAudiences.create(this);
        getDataFolder().mkdirs();
        storageContainer = new StorageContainer();

        BungeeCommandHandler commandHandler = BungeeCommandHandler.create(this);
        commandHandler.register(new DisableCommand());

        // AuthMe 6.0.0+ 使用自定义频道 authme:main 通知跨端登录, 需在 proxy 侧注册才能收到 PluginMessageEvent
        getProxy().registerChannel("authme:main");

        getProxy().getPluginManager().registerListener(this, new PlayerListener());
    }

    @Override
    public void onDisable() {
        // 关停兜底: 同步全量落盘一次, 防止 debounce 队列里的待保存任务被进程退出丢掉.
        try {
            storageContainer.lastServerStore.saveAllHistory();
        } catch (Throwable t) {
            getLogger().warning("BackToLastServer: 保存历史记录失败: " + t);
        }
        // 反注册 authme:main 频道
        getProxy().unregisterChannel("authme:main");
        if (this.adventure != null) {
            this.adventure.close();
            this.adventure = null;
        }
    }

    public @NonNull BungeeAudiences adventure() {
        if (this.adventure == null) {
            throw new IllegalStateException("Cannot retrieve audience provider while plugin is not enabled");
        }
        return this.adventure;
    }

}

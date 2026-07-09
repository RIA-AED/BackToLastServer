package ink.magma.backtolastserver.storage;

import ink.magma.backtolastserver.logger.UniLogger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class LastServerStore {
    // ConcurrentHashMap: BungeeCord / Velocity 的断连事件在多个 Netty IO 线程并发派发,
    // 普通 HashMap 在并发 put + 迭代 (yaml.dump) 时会抛 ConcurrentModificationException.
    public static ConcurrentHashMap<String, String> serverHistory = new ConcurrentHashMap<>();
    static File dataFolder = PluginFolderHandler.getPluginFolder();
    static File historyFile = new File(dataFolder.getPath(), "history.yml");
    static Yaml yaml;

    // Debounce 保存: 多个玩家短时间内断连/登录只真正全量落盘 1 次, 避免 O(N) 次 IO.
    private static final ScheduledExecutorService saveScheduler =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "BackToLastServer-SaveScheduler");
                t.setDaemon(true);
                return t;
            });
    private static final AtomicBoolean savePending = new AtomicBoolean(false);
    private static final long SAVE_DEBOUNCE_SECONDS = 3L;

    public LastServerStore() {
        DumperOptions dumperOptions = new DumperOptions();
        dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        yaml = new Yaml(dumperOptions);

        Map<String, String> historyInFile = readAllHistory();
        if (historyInFile != null && !historyInFile.isEmpty()) {
            UniLogger.info("载入了 " + historyInFile.size() + " 条历史记录.");
            serverHistory = new ConcurrentHashMap<>(historyInFile);
        }
    }

    @Nullable
    public String getLastServerID(String playerUUID) {
        return serverHistory.get(playerUUID);
    }

    public void setHistory(@NotNull String playerUUID, @NotNull String serverID) {
        serverHistory.put(playerUUID, serverID);
    }

    public void removeHistory(@NotNull String playerUUID) {
        serverHistory.remove(playerUUID);
    }

    /**
     * 立即全量保存 (同步, 线程安全). 供关闭 / 兜底使用.
     */
    public synchronized void saveAllHistory() {
        if (!dataFolder.exists()) {
            boolean result = dataFolder.mkdirs();
            if (!result) UniLogger.warn("创建插件配置文件夹失败!");
        }

        try (FileWriter writer = new FileWriter(historyFile)) {
            // dump 前快照一份, 避免 SnakeYAML 遍历期间被其它 IO 线程结构性修改.
            yaml.dump(new HashMap<>(serverHistory), writer);
        } catch (IOException e) {
            UniLogger.warn(e.toString());
        }
    }

    /**
     * Debounce 请求保存: SAVE_DEBOUNCE_SECONDS 秒内若有多次调用, 只真正落盘 1 次.
     * 适合在玩家断连 / 登录后调用, 避免高并发下每个玩家触发一次全量 dump + IO.
     */
    public void requestSave() {
        if (!savePending.compareAndSet(false, true)) {
            // 已有待执行的保存任务, 当前请求被合并到那次里, 跳过.
            return;
        }
        saveScheduler.schedule(() -> {
            // 先重置标志, 这样在本次落盘期间到来的新请求会再起一个新任务, 保证后续改动不丢.
            savePending.set(false);
            saveAllHistory();
        }, SAVE_DEBOUNCE_SECONDS, TimeUnit.SECONDS);
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public Map<String, String> readAllHistory() {
        if (historyFile.exists()) {
            try (FileReader reader = new FileReader(historyFile)) {
                return (Map<String, String>) yaml.load(reader);
            } catch (IOException e) {
                UniLogger.warn(e.toString());
            }
        }
        return null;
    }
}
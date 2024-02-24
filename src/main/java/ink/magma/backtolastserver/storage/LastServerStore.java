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

public class LastServerStore {
    public static HashMap<String, String> serverHistory = new HashMap<>();
    static File dataFolder = PluginFolderHandler.getPluginFolder();
    static File historyFile = new File(dataFolder.getPath(), "history.yml");
    static Yaml yaml;

    public LastServerStore() {
        DumperOptions dumperOptions = new DumperOptions();
        dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        yaml = new Yaml(dumperOptions);

        HashMap<String, String> historyInFile = readAllHistory();
        if (historyInFile != null && !historyInFile.isEmpty()) {
            UniLogger.info("载入了 " + historyInFile.size() + " 条历史记录.");
            serverHistory = historyInFile;
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

    public synchronized void saveAllHistory() {
        if (!dataFolder.exists()) {
            boolean result = dataFolder.mkdirs();
            if (!result) UniLogger.warn("创建插件配置文件夹失败!");
        }

        try (FileWriter writer = new FileWriter(historyFile)) {
            yaml.dump(serverHistory, writer);
        } catch (IOException e) {
            UniLogger.warn(e.toString());
        }
    }

    @Nullable
    public HashMap<String, String> readAllHistory() {
        if (historyFile.exists()) {
            try (FileReader reader = new FileReader(historyFile)) {
                return yaml.load(reader);
            } catch (IOException e) {
                UniLogger.warn(e.toString());
            }
        }
        return null;
    }
}

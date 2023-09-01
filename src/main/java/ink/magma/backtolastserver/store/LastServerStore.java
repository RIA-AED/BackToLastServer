package ink.magma.backtolastserver.store;

import ink.magma.backtolastserver.BackToLastServer;
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
    static File dataFolder = BackToLastServer.dataDirectory.toFile();
    static File historyFile = new File(dataFolder.getPath(), "history.yml");
    static Yaml yaml;

    public LastServerStore() {
        DumperOptions dumperOptions = new DumperOptions();
        dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        yaml = new Yaml(dumperOptions);

        HashMap<String, String> historyInFile = readAllHistory();
        if (historyInFile != null && !historyInFile.isEmpty()) {
            BackToLastServer.logger.info("载入了 " + historyInFile.size() + " 条历史记录.");
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
            if (!result) BackToLastServer.logger.error("创建插件配置文件夹失败!");
        }

        try (FileWriter writer = new FileWriter(historyFile)) {
            yaml.dump(serverHistory, writer);
        } catch (IOException e) {
            BackToLastServer.logger.error(e.toString());
        }
    }

    @Nullable
    public HashMap<String, String> readAllHistory() {
        if (historyFile.exists()) {
            try (FileReader reader = new FileReader(historyFile)) {
                return yaml.load(reader);
            } catch (IOException e) {
                BackToLastServer.logger.error(e.toString());
            }
        }
        return null;
    }
}

package ink.magma.backtolastserver.storage;

import ink.magma.backtolastserver.logger.UniLogger;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class DisableServerStore {
    public static ArrayList<String> disableServerList = new ArrayList<>();

    static File dataFolder = PluginFolderHandler.getPluginFolder();
    static File file = new File(dataFolder.getPath(), "disable-server.yml");
    static Yaml yaml = new Yaml();

    public DisableServerStore() {
        ArrayList<String> readResult = readFromFile();
        if (readResult != null && !readResult.isEmpty()) {
            UniLogger.info("载入了 " + readResult.size() + " 条禁用服务器记录.");
            disableServerList = readResult;
        } else {
            saveToFile();
        }
    }

    public void saveToFile() {
        if (!dataFolder.exists()) dataFolder.mkdirs();

        String yamlString = yaml.dump(disableServerList);

        try (FileWriter writer = new FileWriter(file)) {
            writer.write(yamlString);
        } catch (IOException e) {
            UniLogger.warn(e.toString());
        }
    }

    @Nullable
    public ArrayList<String> readFromFile() {
        if (file.exists()) {
            try (FileReader reader = new FileReader(file)) {
                return yaml.load(reader);
            } catch (IOException e) {
                UniLogger.warn(e.toString());
            }
        }
        return null;
    }

}

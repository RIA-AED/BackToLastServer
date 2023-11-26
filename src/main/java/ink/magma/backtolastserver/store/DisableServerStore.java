package ink.magma.backtolastserver.store;

import ink.magma.backtolastserver.BackToLastServer;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class DisableServerStore {
    public static ArrayList<String> disableServerList = new ArrayList<>();

    static File dataFolder = BackToLastServer.dataDirectory.toFile();
    static File file = new File(dataFolder.getPath(), "disable-server.yml");
    static Yaml yaml = new Yaml();

    public DisableServerStore() {
        ArrayList<String> readResult = readFromFile();
        if (readResult != null && !readResult.isEmpty()) {
            BackToLastServer.logger.info("载入了 " + readResult.size() + " 条禁用记录.");
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
            BackToLastServer.logger.error(e.toString());
        }
    }

    @Nullable
    public ArrayList<String> readFromFile() {
        if (file.exists()) {
            try (FileReader reader = new FileReader(file)) {
                return yaml.load(reader);
            } catch (IOException e) {
                BackToLastServer.logger.error(e.toString());
            }
        }
        return null;
    }

}

package ink.magma.backtolastserver.store;

import ink.magma.backtolastserver.BackToLastServer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class DisableStore {
    public static Set<String> disableSet = new HashSet<>();
    static File dataFolder = BackToLastServer.dataDirectory.toFile();
    static File disableFile = new File(dataFolder.getPath(), "disable-users.yml");
    static Yaml yaml = new Yaml();

    public DisableStore() {
        Set<String> disableInFile = readAllDisable();
        if (disableInFile != null && !disableInFile.isEmpty()) {
            BackToLastServer.logger.info("载入了 " + disableInFile.size() + " 条禁用记录.");
            disableSet = disableInFile;
        }
    }

    /**
     * 获取玩家是否允许自动返回上一个服务器.
     * 如果玩家没有设置过, 那么默认是 true.
     *
     * @return 是否允许
     */
    @NotNull
    public Boolean getIsEnable(String playerUUID) {
        return !disableSet.contains(playerUUID);
    }

    /**
     * 禁用掉玩家的自动返回上一个服务器
     *
     * @param setDisable 操作禁用:true  操作启用:false
     */
    public void setDisable(@NotNull String playerUUID, boolean setDisable) {
        if (setDisable) {
            disableSet.add(playerUUID);
        } else {
            disableSet.remove(playerUUID);
        }
    }

    public void saveAllDisable() {
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        String yamlString = yaml.dump(disableSet);

        try (FileWriter writer = new FileWriter(disableFile)) {
            writer.write(yamlString);
        } catch (IOException e) {
            BackToLastServer.logger.error(e.toString());
        }
    }

    @Nullable
    public Set<String> readAllDisable() {
        if (disableFile.exists()) {
            try (FileReader reader = new FileReader(disableFile)) {
                return yaml.load(reader);
            } catch (IOException e) {
                BackToLastServer.logger.error(e.toString());
            }
        }
        return null;
    }
}

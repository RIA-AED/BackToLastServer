package ink.magma.backtolastserver.storage;

import net.william278.annotaml.YamlComment;
import net.william278.annotaml.YamlFile;

import java.util.List;

@YamlFile
public class EnableServersConfig {

    @YamlComment("是否默认允许所有子服务器可被自动返回")
    public boolean allowAllServer = false;

    @YamlComment("允许自动返回的子服列表")
    public List<String> allowServers = List.of("zeroth", "naraku", "houtu");

    public boolean getIsAllowed(String serverId) {
        if (allowAllServer) return true;
        return allowServers.contains(serverId);
    }

    public EnableServersConfig() {
    }
}

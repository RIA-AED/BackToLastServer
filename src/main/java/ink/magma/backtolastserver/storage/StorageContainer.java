package ink.magma.backtolastserver.storage;

import net.william278.annotaml.Annotaml;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class StorageContainer {
    public LastServerStore lastServerStore;
    public DisableStore disableStore;
    public EnableServersConfig enableServersConfig;


    public StorageContainer() {
        lastServerStore = new LastServerStore();
        disableStore = new DisableStore();

        createEnableServersConfig();
    }

    public void createEnableServersConfig() throws IllegalStateException {
        try {
            final Annotaml<EnableServersConfig> annotaml = Annotaml.create(PluginFolderHandler.getFileInPluginFolder("./enable-servers.yml"), new EnableServersConfig());

            this.enableServersConfig = annotaml.get();
        } catch (IOException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new IllegalStateException("Failed to create config", e);
        }
    }
}

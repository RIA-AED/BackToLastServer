package ink.magma.backtolastserver.storage;

public class StorageContainer {
    public LastServerStore lastServerStore;
    public DisableStore disableStore;
    public DisableServerStore disableServerStore;

    public StorageContainer() {
        lastServerStore = new LastServerStore();
        disableStore = new DisableStore();
        disableServerStore = new DisableServerStore();
    }
}

package ink.magma.backtolastserver.platform;

public class PlatformHandler {
    private static PlatformType platformType;

    public static void setPlatformType(PlatformType type) {
        platformType = type;
    }

    public static PlatformType getPlatformType() {
        return platformType;
    }

    public enum PlatformType {
        Bungee,
        Velocity
    }
}

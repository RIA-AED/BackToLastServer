package ink.magma.backtolastserver.logger;

import ink.magma.backtolastserver.platform.PlatformHandler;
import ink.magma.backtolastserver.platform.bungee.BackToLastServerBungee;
import ink.magma.backtolastserver.platform.velocity.BackToLastServerVelocity;

public class UniLogger {
    public static void info(String message) {
        if (PlatformHandler.getPlatformType() == PlatformHandler.PlatformType.Bungee) {
            BackToLastServerBungee.instance.getLogger().info(message);
        }
        if (PlatformHandler.getPlatformType() == PlatformHandler.PlatformType.Velocity) {
            BackToLastServerVelocity.logger.info(message);
        }
    }

    public static void warn(String message) {
        if (PlatformHandler.getPlatformType() == PlatformHandler.PlatformType.Bungee) {
            BackToLastServerBungee.instance.getLogger().warning(message);
        }
        if (PlatformHandler.getPlatformType() == PlatformHandler.PlatformType.Velocity) {
            BackToLastServerVelocity.logger.warn(message);
        }
    }
}

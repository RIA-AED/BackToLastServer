package ink.magma.backtolastserverbukkit.Listener;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import fr.xephi.authme.events.LoginEvent;
import ink.magma.backtolastserverbukkit.BackToLastServer_Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class LoginListener implements Listener {
    @EventHandler
    public void onAuthmeLogin(LoginEvent event) {
        Player player = event.getPlayer();

        ByteArrayDataOutput output = ByteStreams.newDataOutput();
        output.writeUTF(player.getUniqueId().toString());

        player.sendPluginMessage(BackToLastServer_Bukkit.instance, "back-to-last-server:authme-login", output.toByteArray());
    }
}

package rudynakodach.github.io.webhookintegrations.Events;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import rudynakodach.github.io.webhookintegrations.Modules.MessageConfiguration;
import rudynakodach.github.io.webhookintegrations.Modules.MessageType;
import rudynakodach.github.io.webhookintegrations.WebhookActions;
import rudynakodach.github.io.webhookintegrations.WebhookIntegrations;

import java.text.SimpleDateFormat;
import java.util.*;

public class onPlayerJoin implements Listener {

    JavaPlugin plugin;
    Collection<String> opsJoined = new ArrayList<>();
    public onPlayerJoin(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
        sendOpMessage(event);

        if (!MessageConfiguration.get().canAnnounce(MessageType.PLAYER_JOIN.getValue())) {
            return;
        }

        String json = MessageConfiguration.get().getMessage(MessageType.PLAYER_JOIN.getValue());

        if(json == null) {
            return;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        sdf.setTimeZone(TimeZone.getTimeZone(plugin.getConfig().getString("timezone")));

        json = json.replace("$playersOnline$", String.valueOf(plugin.getServer().getOnlinePlayers().size()))
                .replace("$timestamp$", sdf.format(new Date()))
                .replace("$maxPlayers$", String.valueOf(plugin.getServer().getMaxPlayers()))
                .replace("$uuid$", event.getPlayer().getUniqueId().toString())
                .replace("$player$", event.getPlayer().getName())
                .replace("$time$", new SimpleDateFormat(
                        Objects.requireNonNullElse(
                                plugin.getConfig().getString("date-format"),
                                "")).format(new Date())
                );

        if(plugin.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            json = PlaceholderAPI.setPlaceholders(event.getPlayer(), json);
        }

        new WebhookActions(plugin).SendAsync(json);
    }

    private void sendOpMessage(PlayerJoinEvent event) {
        if (!opsJoined.contains(event.getPlayer().getName())) {
            if (plugin.getServer().getOperators().contains(event.getPlayer())) {
                if (!WebhookIntegrations.isLatest) {
                    event.getPlayer().sendMessage(ChatColor.GRAY + "[" + ChatColor.BLUE + "W" + ChatColor.WHITE + "I" + ChatColor.GRAY + "]" + ChatColor.WHITE + " Update available. Please update from either GitHub, SpigotMC or Bukkit.");
                    opsJoined.add(event.getPlayer().getName());
                }
            }
        }
    }
}

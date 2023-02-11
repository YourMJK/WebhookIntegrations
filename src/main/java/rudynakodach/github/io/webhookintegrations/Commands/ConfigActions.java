package rudynakodach.github.io.webhookintegrations.Commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import rudynakodach.github.io.webhookintegrations.WebhookIntegrations;

import java.util.ArrayList;
import java.util.List;

public class ConfigActions implements CommandExecutor, TabCompleter {
    JavaPlugin plugin;
    public ConfigActions(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        Player player = (Player) commandSender;
        if(command.getName().equalsIgnoreCase("wi")) {
            if (args.length >= 1) {
                if (args[0].equalsIgnoreCase("reset")) {
                    if (args[1].equalsIgnoreCase("confirm")) {
                        if (player.hasPermission("webhookintegrations.config.reset")) {
                            plugin.saveResource("config.yml", true);
                            plugin.reloadConfig();
                            return true;
                        }
                    } else {
                        commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', WebhookIntegrations.lang.getString(WebhookIntegrations.localeLang + ".commands.config.noConfirm")));
                    }
                } else if (args[0].equalsIgnoreCase("reload")) {
                    if (player.hasPermission("webhookintegrations.reload")) {
                        plugin.reloadConfig();
                        commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', WebhookIntegrations.lang.getString(WebhookIntegrations.localeLang + ".commands.config.reloadFinish")));
                        return true;
                    }
                } else if(args[0].equalsIgnoreCase("analyze")) {
                    commandSender.sendMessage(ChatColor.AQUA + "Analyzing config... To reload the config use /wi reload");
                    StringBuilder message = new StringBuilder("auto-update: " + colorBoolean(plugin.getConfig().getBoolean("auto-update")));
                    if(plugin.getConfig().getString("webhookUrl").trim().equalsIgnoreCase("")) {
                        message.append("\nwebhookUrl: " + ChatColor.RED + "unset\n");
                    }
                    else {
                        message.append("\nwebhookUrl: " + ChatColor.GREEN + "set\n");
                    }
                    message.append(ChatColor.YELLOW + "EVENTS\n");
                    message.append("");
                }
            }
            else {
                if(player.hasPermission("webhookintegrations.wi")) {
                    String text = "/wi\n\treset\n\t\t&cconfirm&r\n\treload";
                    text = text.replace("\n", "  ");
                    commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            text));
                }
            }
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        List<String> suggestions = new ArrayList<>();
        if(command.getName().equalsIgnoreCase("wi")) {

            if (args.length == 0) {
                suggestions.add("reset");
                suggestions.add("reload");
                suggestions.add("analyze");
                return suggestions;
            } else {
                if (args.length == 1) {
                    if (args[0].equalsIgnoreCase("reset")) {
                        suggestions.add("confirm");
                        return suggestions;
                    }
                }
            }
        }
        return null;
    }

    @Contract(pure = true)
    private @NotNull String colorBoolean(Boolean b) {
        if(!b) {
            return ChatColor.RED + "false";
        }
        else {
            return ChatColor.GREEN + "true";
        }
    }
}

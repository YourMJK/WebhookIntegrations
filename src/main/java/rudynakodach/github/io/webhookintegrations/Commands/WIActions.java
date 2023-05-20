package rudynakodach.github.io.webhookintegrations.Commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import rudynakodach.github.io.webhookintegrations.AutoUpdater;
import rudynakodach.github.io.webhookintegrations.Modules.LanguageConfiguration;
import rudynakodach.github.io.webhookintegrations.Modules.MessageConfiguration;
import rudynakodach.github.io.webhookintegrations.WebhookIntegrations;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

public class WIActions implements CommandExecutor, TabCompleter {
    JavaPlugin plugin;
    private final LanguageConfiguration language;
    public WIActions(JavaPlugin plugin) {
        this.plugin = plugin;

        language = LanguageConfiguration.get();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if(!(commandSender instanceof Player player)) {
            plugin.getLogger().log(Level.INFO, "This command is intended to be used in the game.");
            return true;
        }
        if(command.getName().equalsIgnoreCase("wi")) {
            if (args.length >= 1) {
                if (args[0].equalsIgnoreCase("reset")) {
                    if (args[1].equalsIgnoreCase("confirm")) {
                        return resetConfirm(commandSender);
                    } else {
                        if(!player.hasPermission("webhookintegrations.config.reset")) {
                            player.sendMessage(
                                    ChatColor.translateAlternateColorCodes('&',
                                            language.getString("commands.no-permission"))
                            );
                            return true;
                        }
                        commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', language.getString("commands.config.noConfirm")));
                        return true;
                    }
                } else if (args[0].equalsIgnoreCase("reload")) {
                    return reload(commandSender);
                } else if(args[0].equalsIgnoreCase("analyze")) {
                    return analyze(commandSender);
                } else if (args[0].equalsIgnoreCase("update")) {
                    return update(commandSender);
                } else if(args[0].equalsIgnoreCase("enable")) {
                    return enable(commandSender);
                } else if(args[0].equalsIgnoreCase("disable")) {
                    return disable(commandSender);
                } else if(args[0].equalsIgnoreCase("setlanguage")) {
                    return setLanguage(commandSender, args);
                } else if(args[0].equalsIgnoreCase("config") && args.length >= 3) {
                    return setConfig(commandSender, args);
                }
            }
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        List<String> suggestions = new ArrayList<>();
        if (command.getName().equalsIgnoreCase("wi")) {
            if (args.length == 1) {
                suggestions.add("setlanguage");
                suggestions.add("reset");
                suggestions.add("enable");
                suggestions.add("disable");
                suggestions.add("reload");
                suggestions.add("analyze");
                suggestions.add("update");
                suggestions.add("config");
            } else if (args.length == 2) {
                if(args[0].equalsIgnoreCase("reset")) {
                    suggestions.add("confirm");
                } else if(args[0].equalsIgnoreCase("setlanguage")) {
                    return language.getYamlConfig().getKeys(false).stream().toList();
                } else if(args[0].equalsIgnoreCase("config")) {
                    suggestions.add("setvalue");
                    return suggestions;
                }
            } else if(args.length == 3) {
                if(args[0].equalsIgnoreCase("config") && args[1].equalsIgnoreCase("setvalue")) {
                    return plugin.getConfig().getKeys(true).stream().toList();
                }
            } else if(args.length == 4) {
                if(args[0].equalsIgnoreCase("config") && args[1].equalsIgnoreCase("setvalue")) {
                    if(!plugin.getConfig().contains(args[3])) {return null;}
                    Object value = plugin.getConfig().get(args[3]);
                    if(value instanceof Boolean) {
                        suggestions.add("true");
                        suggestions.add("false");
                        return suggestions;
                    }
                }
            }
        }
        return suggestions;
    }

    @Contract(pure = true)
    private @NotNull String colorBoolean(Boolean b) {
        if(!b) {
            return ChatColor.RED + "" + ChatColor.BOLD + false + ChatColor.RESET;
        }
        else {
            return ChatColor.GREEN + "" + ChatColor.BOLD + true + ChatColor.RESET;
        }
    }

    private boolean analyze(CommandSender commandSender) {
        Player player = (Player) commandSender;
        if (!player.hasPermission("webhookintegrations.analyze")) {
            player.sendMessage(
                    ChatColor.translateAlternateColorCodes('&',
                            language.getString("commands.no-permission"))
            );
            return true;
        }
        commandSender.sendMessage(ChatColor.AQUA + "Analyzing config... To reload the config use /wi reload");
        String message = "auto-update: " + colorBoolean(plugin.getConfig().getBoolean("auto-update"));
        if (Objects.requireNonNull(plugin.getConfig().getString("webhookUrl")).trim().equalsIgnoreCase("")) {
            message += "\nwebhookUrl: " + ChatColor.RED + "unset\n";
        } else {
            message += "\nwebhookUrl: " + ChatColor.GREEN + "set\n";
        }
        message += ChatColor.YELLOW + "EVENTS" + ChatColor.WHITE;
        message += "\nonStart: " + colorBoolean(plugin.getConfig().getBoolean("onServerStart.announce"));
        message += "\nonStop: " + colorBoolean(plugin.getConfig().getBoolean("onServerStop.announce"));
        message += "\nplayerJoin: " + colorBoolean(plugin.getConfig().getBoolean("onPlayerJoin.announce"));
        message += "\nplayerQuit: " + colorBoolean(plugin.getConfig().getBoolean("onPlayerQuit.announce"));
        message += "\nplayerKicked: " + colorBoolean(plugin.getConfig().getBoolean("onPlayerKicked.announce"));
        message += "\nonAdvancementMade: " + colorBoolean(plugin.getConfig().getBoolean("onPlayerAdvancementComplete.announce"));
        message += "\nplayerDeathPve: " + colorBoolean(plugin.getConfig().getBoolean("onPlayerDeath.playerKilledByNPC.announce"));
        message += "\nPlayerDeathPvp: " + colorBoolean(plugin.getConfig().getBoolean("onPlayerDeath.playerKilledByPlayer.announce"));

        commandSender.sendMessage(message);
        return true;
    }

    private boolean resetConfirm(CommandSender commandSender) {
        Player player = (Player) commandSender;
        if (!player.hasPermission("webhookintegrations.config.reset")) {
            player.sendMessage(
                    ChatColor.translateAlternateColorCodes('&',
                            language.getString("commands.no-permission"))
            );
            return true;
        }
        plugin.saveResource("config.yml", true);
        plugin.reloadConfig();
        return true;
    }

    private boolean reload(CommandSender commandSender) {
        Player player = (Player) commandSender;
        if (!player.hasPermission("webhookintegrations.reload")) {
            player.sendMessage(
                    ChatColor.translateAlternateColorCodes('&',
                            language.getString("commands.no-permission"))
            );
            return true;
        }
        plugin.reloadConfig();
        YamlConfiguration languageFile = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "lang.yml"));
        language.reload(languageFile);
        MessageConfiguration.get().reload();
        commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', language.getString("commands.config.reloadFinish")));
        return true;
    }

    private boolean update(CommandSender commandSender) {
        Player player = (Player) commandSender;
        if(!player.hasPermission("webhookintegrations.update")) {
            player.sendMessage(
                    ChatColor.translateAlternateColorCodes('&',
                            language.getString("commands.no-permission"))
            );
            return true;
        }
        AutoUpdater updater = new AutoUpdater(plugin);
        try {
            int latestVersion = updater.getLatestVersion();
            if (latestVersion > WebhookIntegrations.currentBuildNumber) {
                boolean success = updater.Update();
                if (success) {
                    commandSender.sendMessage(language.getString("commands.update.success"));
                } else {
                    commandSender.sendMessage(language.getString("commands.update.failed"));
                }
            } else {
                if(latestVersion == -1) {
                    commandSender.sendMessage(language.getString("commands.update.versionCheckFailed"));
                } else {
                    commandSender.sendMessage(language.getString("commands.update.latest"));
                }
            }
        } catch (IOException ignored) {}
        return true;
    }

    private boolean enable(CommandSender commandSender) {
        Player player = (Player) commandSender;
        if(!player.hasPermission("webhookintegrations.enable")) {
            player.sendMessage(
                    ChatColor.translateAlternateColorCodes('&',
                            language.getString("commands.no-permission"))
            );
            return true;
        }
        plugin.getConfig().set("isEnabled", true);
        try {
            plugin.getConfig().save(new File(plugin.getDataFolder(), "config.yml"));
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Couldn't enable webhook due to an exception: " + e.getMessage());
        }
        plugin.reloadConfig();
        return true;
    }

    private boolean disable(CommandSender commandSender) {
        Player player = (Player) commandSender;
        if(!player.hasPermission("webhookintegrations.disable")) {
            player.sendMessage(
                    ChatColor.translateAlternateColorCodes('&',
                            language.getString("commands.no-permission"))
            );
            return true;
        }
        plugin.getConfig().set("isEnabled", false);
        try {
            plugin.getConfig().save(new File(plugin.getDataFolder(), "config.yml"));
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Couldn't disable webhook due to an exception: " + e.getMessage());
        }
        plugin.reloadConfig();
        return true;
    }

    private boolean setLanguage(CommandSender commandSender, String[] args) {
        Player player = (Player) commandSender;
        if(!player.hasPermission("webhookintegrations.setlanguage")) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                language.getString("commands.no-permission")));
            return true;
        }
        String newLang = args[1];
        if(language.getYamlConfig().contains(newLang)) {
            language.setLanguage(newLang);
            commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', language.getString("commands.setLang.changed")));
        } else {
            commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', language.getString("commands.setLang.notExists")));
        }
        return true;
    }

    private boolean setConfig(CommandSender commandSender, String[] args) {
        Player player = (Player) commandSender;
        if(!player.hasPermission("webhookintegrations.config.setvalue")) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    language.getString("commands.no-permission")));
            return true;
        }
        String path = args[2];
        Object value = args.length >= 4 ? String.join(" ", Arrays.copyOfRange(args, 3, args.length)) : null;
        String message;
        Object oldValue = null;

        if(plugin.getConfig().contains(path)) {
            oldValue = plugin.getConfig().get(path);
            message = LanguageConfiguration.get().getString("commands.config.keyEdited");
        } else {
            message = LanguageConfiguration.get().getString("commands.config.keyCreated");
        }

        if(value == null) {
            message = LanguageConfiguration.get().getString("commands.config.keyRemoved");
        } else {
            if (value.toString().equalsIgnoreCase("true")) {
                value = true;
            } else if (value.toString().equalsIgnoreCase("false")) {
                value = false;
            } else {
                try {
                    value = Integer.parseInt(value.toString());
                } catch (NumberFormatException ignored) {}
            }
        }

        plugin.getConfig().set(path, value);
        try {
            plugin.getConfig().save(new File(plugin.getDataFolder(), "config.yml"));
        } catch (Exception e) {
            commandSender.sendMessage(LanguageConfiguration.get().getString("commands.config.saveFailed").replace("%04", e.getMessage()));
            return true;
        }

        message = message.replace("%01", path)
                .replace("%02", value instanceof String ?
                        String.format("\"%s\"", value) :
                        String.valueOf(value));

        if(oldValue != null) {
            message = message.replace("%03", oldValue.toString());
        }

        commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
        return true;
    }

}

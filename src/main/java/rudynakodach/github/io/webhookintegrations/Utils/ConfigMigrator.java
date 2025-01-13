package rudynakodach.github.io.webhookintegrations.Utils;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;
import rudynakodach.github.io.webhookintegrations.Modules.MessageConfiguration;
import rudynakodach.github.io.webhookintegrations.Modules.MessageType;
import rudynakodach.github.io.webhookintegrations.Modules.TemplateConfiguration;

import java.util.logging.Level;

public class ConfigMigrator {
    public static void migrate(JavaPlugin plugin, int current, int target) {
        plugin.getLogger().log(Level.INFO, "Migrating config from %d to %d".formatted(current, target));

        switch (target) {
            case 2:
                if(current == 1) {
                    toVersion2(plugin);
                }
                break;
        }

    }

    private static void toVersion2(JavaPlugin plugin) {
        String webhookUrl = plugin.getConfig().getString("webhookUrl");
        if(webhookUrl != null) {
            if(!webhookUrl.isEmpty()) {
                plugin.getConfig().set("webhooks.main", webhookUrl);
            }
        }

        MessageConfiguration messageConfiguration = MessageConfiguration.get();
        for(String messageType : MessageType.getAllMessageTypes()) {
            ConfigurationSection sect = messageConfiguration.config.getConfigurationSection(messageType);

            if(sect != null) {
                sect.set("target", "main");
            }
        }
        if(!messageConfiguration.save()) {
            plugin.getLogger().log(Level.WARNING, "Failed to save message config file during migration");
        }
        messageConfiguration.reload();

        TemplateConfiguration templateConfiguration = TemplateConfiguration.get();
        ConfigurationSection templates = templateConfiguration.config.getConfigurationSection("templates");

        if(templates != null) {
            for(String temp : templates.getKeys(false)) {
                templates.set("%s.defaultTarget".formatted(temp), "main");
            }
        }
        if(!templateConfiguration.save()) {
            plugin.getLogger().log(Level.INFO, "Failed to save template config file during migration");
        }
        templateConfiguration.reload();

        plugin.getConfig().set("remove-color-coding", false);
        plugin.getConfig().set("color-coding-regex", "[&§][a-f0-9klmnor]|&?#[0-9a-f]{6}");

        plugin.getConfig().set("config-version", 2);
        plugin.saveConfig();
        plugin.reloadConfig();

        plugin.getLogger().log(Level.INFO, "Config migrated to version 2!");
    }
}

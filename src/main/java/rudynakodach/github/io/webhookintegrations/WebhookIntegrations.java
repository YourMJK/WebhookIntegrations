package rudynakodach.github.io.webhookintegrations;

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import rudynakodach.github.io.webhookintegrations.Commands.*;
import rudynakodach.github.io.webhookintegrations.Events.*;
import rudynakodach.github.io.webhookintegrations.Modules.LanguageConfiguration;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.logging.Level;

public final class WebhookIntegrations extends JavaPlugin {
    // Welcome, fellow source code reader!
    public static boolean isLatest = true;
    public static int currentBuildNumber = 32;

    //on startup
    @Override
    public void onEnable() {
        saveDefaultConfig();
        getLogger().log(Level.INFO, "Hello, World!");

        if(!new File(getDataFolder(), "lang.yml").exists()) {
            this.saveResource("lang.yml",false);
        }
        getLogger().log(Level.INFO,"Initializing language...");
        File langFile = new File(this.getDataFolder(),"lang.yml");

        Locale locale = Locale.getDefault();
        String selectedLanguage = locale.toString();


        YamlConfiguration languageConfig = YamlConfiguration.loadConfiguration(langFile);

        if(getConfig().get("language-override") instanceof String languageOverride) {
            if(!languageConfig.contains(selectedLanguage)) {
                getLogger().log(Level.INFO, "Language override is set to a language that doesn't exist. Falling back to en_US. For all available languages see lang.yml.");
                selectedLanguage = "en_US";
            } else {
                getLogger().log(Level.INFO, "Language override set to: " + languageOverride);
                selectedLanguage = languageOverride;
            }
        } else {
            if(!languageConfig.contains(selectedLanguage)) {
                selectedLanguage = "en_US";
            }
            getLogger().log(Level.INFO,"Hooked to " + selectedLanguage);
        }

        LanguageConfiguration language = new LanguageConfiguration(selectedLanguage, languageConfig);

        getLogger().log(Level.FINEST, language.getString("onStart.message"));

        if(getConfig().getBoolean("check-for-updates")) {
            getLogger().log(Level.INFO, language.getString("update.checking"));

            try {
                int receivedBuildNumber = new AutoUpdater(this).getLatestVersion();
                if (currentBuildNumber < receivedBuildNumber && receivedBuildNumber != -1) {
                    isLatest = false;
                    getLogger().log(Level.WARNING, "------------------------- WI -------------------------");
                    getLogger().log(Level.INFO, language.getString("update.updateFound"));
                    getLogger().log(Level.WARNING, "------------------------------------------------------");

                    if (getConfig().getBoolean("auto-update")) {
                        isLatest = new AutoUpdater(this).Update();
                    }

                } else {
                    getLogger().log(Level.INFO, language.getString("update.latest"));
                }
            } catch (IOException e) {
                getLogger().log(Level.WARNING, language.getString("update.checkFailed") + e.getMessage());
            }
        }

        this.saveDefaultConfig();

        if (Objects.equals(Objects.requireNonNull(getConfig().getString("webhookUrl")).trim(), "")) {
            getLogger().log(Level.WARNING, language.getString("onStart.webhookEmpty"));
        }

        getLogger().log(Level.INFO, language.getString("onStart.registeringEvents"));

        onPlayerChat chatEvent = new onPlayerChat(this);
        getServer().getPluginManager().registerEvents(chatEvent, this);

        onPlayerJoin onPlayerJoinEvent = new onPlayerJoin(this);
        getServer().getPluginManager().registerEvents(onPlayerJoinEvent, this);

        onPlayerQuit playerQuitEvent = new onPlayerQuit(this);
        getServer().getPluginManager().registerEvents(playerQuitEvent, this);

        onPlayerKick playerKick = new onPlayerKick(this);
        getServer().getPluginManager().registerEvents(playerKick, this);

        onPlayerAdvancementCompleted onPlayerAdvancement = new onPlayerAdvancementCompleted(this);
        getServer().getPluginManager().registerEvents(onPlayerAdvancement, this);

        onPlayerDeath playerDeath = new onPlayerDeath(this);
        getServer().getPluginManager().registerEvents(playerDeath,this);

        getLogger().log(Level.INFO, language.getString("onStart.eventRegisterFinish"));

        SetWebhookURL setWebhookUrlCommand = new SetWebhookURL(getConfig(), this);
        Objects.requireNonNull(getCommand("setUrl")).setExecutor(setWebhookUrlCommand);

        SendToWebhook sendToWebhookCommand = new SendToWebhook(getConfig(), this);
        Objects.requireNonNull(getCommand("send")).setExecutor(sendToWebhookCommand);

        WIActions resetConfig = new WIActions(this);
        Objects.requireNonNull(getCommand("wi")).setExecutor(resetConfig);

        getLogger().log(Level.INFO, language.getString("onStart.commandRegisterFinish"));

        if(getConfig().getBoolean("onServerStart.announce")) {
            sendStartMessage();
        }
    }

    //on shutdown
    @Override
    public void onDisable() {
        if (getConfig().getBoolean("onServerStop.announce")) {
            if (!getConfig().getString("webhookUrl").trim().equals("")) {
                sendStopMessage();
            }
        }

        getLogger().log(Level.INFO, "this is my final message");
        getLogger().log(Level.INFO, "goodb ye");
    }

    private void sendStartMessage() {
        String json = getConfig().getString("onServerStart.messageJson");

        String serverIp = getServer().getIp();
        int slots = getServer().getMaxPlayers();
        String serverMotd = PlainTextComponentSerializer.plainText().serialize(getServer().motd());
        String serverName = getServer().getName();
        String serverVersion = getServer().getVersion();
        Boolean isOnlineMode = getServer().getOnlineMode();
        int playersOnline = getServer().getOnlinePlayers().size();

        json = json.replace("%time%", new SimpleDateFormat("HH:mm:ss").format(new Date()))
            .replace("%serverIp%", serverIp)
            .replace("%maxPlayers%", String.valueOf(slots))
            .replace("%serverMotd%", serverMotd)
            .replace("%serverName%", serverName)
            .replace("%serverVersion%", serverVersion)
            .replace("%isOnlineMode%", String.valueOf(isOnlineMode))
            .replace("%playersOnline%", String.valueOf(playersOnline));

        new WebhookActions(this).SendSync(json);
    }

    private void sendStopMessage() {
        String serverIp = getServer().getIp();
        int slots = getServer().getMaxPlayers();
        String serverMotd = getServer().getMotd();
        String serverName = getServer().getName();
        String serverVersion = getServer().getVersion();
        Boolean isOnlineMode = getServer().getOnlineMode();
        int playersOnline = getServer().getOnlinePlayers().size();

        String json = getConfig().getString("onServerStop.messageJson");

        json = json.replace("%time%", new SimpleDateFormat("HH:mm:ss").format(new Date()))
            .replace("%serverIp%", serverIp)
            .replace("%maxPlayers%", String.valueOf(slots))
            .replace("%serverMotd%", serverMotd)
            .replace("%serverName%", serverName)
            .replace("%serverVersion%", serverVersion)
            .replace("%isOnlineMode%", String.valueOf(isOnlineMode))
            .replace("%playersOnline%", String.valueOf(playersOnline));

        new WebhookActions(this).SendSync(json);
    }
}
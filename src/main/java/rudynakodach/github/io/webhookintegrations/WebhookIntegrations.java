package rudynakodach.github.io.webhookintegrations;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.bukkit.plugin.java.JavaPlugin;
import rudynakodach.github.io.webhookintegrations.Commands.SendToWebhook;
import rudynakodach.github.io.webhookintegrations.Commands.SetWebhookURL;
import rudynakodach.github.io.webhookintegrations.Events.*;

import java.io.IOException;
import java.util.Objects;
import java.util.logging.Level;

public final class WebhookIntegrations extends JavaPlugin {

    public static int currentBuildNumber = 4;
    static String buildNumberUrl = "https://raw.githubusercontent.com/rudynakodach/WebhookIntegrations/master/buildnumber";

    //on startup
    @Override
    public void onEnable() {

        getLogger().log(Level.INFO, "Hello, World!");

        getLogger().log(Level.INFO, "Checking for updates...");

        int receivedBuildNumber = getVersion();
        if (currentBuildNumber < receivedBuildNumber && receivedBuildNumber != -1) {
            Component text = Component.text("New version available on the GitHub repository. Please update.", NamedTextColor.GREEN);
            getComponentLogger().info(text);
        }

        this.saveDefaultConfig();



        if (Objects.equals(Objects.requireNonNull(getConfig().getString("webhookUrl")).trim(), "")) {
            getLogger().log(Level.WARNING, "WebhookURL is empty and cannot be used! Set the value of webhookUrl inside the oldconfig.yml file and restart the server or use \"/seturl <url>\"!");
        }

        getLogger().log(Level.INFO,"Registering events...");

        onPlayerChat chatEvent = new onPlayerChat(this);
        getServer().getPluginManager().registerEvents(chatEvent,this);

        onPlayerJoin onPlayerJoinEvent = new onPlayerJoin(this);
        getServer().getPluginManager().registerEvents(onPlayerJoinEvent, this);

        onPlayerQuit playerQuitEvent = new onPlayerQuit(this);
        getServer().getPluginManager().registerEvents(playerQuitEvent,this);

        onPlayerKick playerKick = new onPlayerKick(this);
        getServer().getPluginManager().registerEvents(playerKick, this);

        onPlayerAdvancementCompleted onPlayerAdvancement = new onPlayerAdvancementCompleted(this);
        getServer().getPluginManager().registerEvents(onPlayerAdvancement, this);

        getLogger().log(Level.INFO, "Events registered.");

        SetWebhookURL setWebhookUrlCommand = new SetWebhookURL(getConfig(), this, getLogger());
        Objects.requireNonNull(getCommand("seturl")).setExecutor(setWebhookUrlCommand);

        SendToWebhook sendToWebhookCommand = new SendToWebhook(getConfig(), this, getLogger());
        Objects.requireNonNull(getCommand("send")).setExecutor(sendToWebhookCommand);
        getLogger().log(Level.INFO, "Commands registered.");

    }

    //on shutdown
    @Override
    public void onDisable() {
        getLogger().log(Level.INFO, "this is my final message");
        getLogger().log(Level.INFO, "goodb ye");
    }

    public Integer getVersion() {

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(buildNumberUrl)
                .get()
                .build();
        try (Response response = client.newCall(request).execute()) {

            if (response.isSuccessful()) {
                String body = response.body().string();
                body = body.trim();
                body = body.replaceAll("[\r\n\t]", "");
                int receivedBuildNumber = Integer.parseInt(body);
                response.close();
                return receivedBuildNumber;
            }

        } catch (IOException e) {
            getLogger().log(Level.WARNING, "Failed to get build number: " + e.getMessage());
        }
        return -1;
    }
}

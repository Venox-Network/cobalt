package network.venox.cobalt;

import net.dv8tion.jda.api.requests.GatewayIntent;

import network.venox.cobalt.listeners.*;

import org.jetbrains.annotations.NotNull;

import xyz.srnyx.javautilities.FileUtility;

import xyz.srnyx.lazylibrary.LazyEmbed;
import xyz.srnyx.lazylibrary.LazyLibrary;

import java.io.File;
import java.nio.file.Path;


public class Cobalt extends LazyLibrary {
    public CoConfig config;
    public DataManager dataManager;

    public Cobalt() {
        // TTS
        System.setProperty("freetts.voices", "com.sun.speech.freetts.en.us.cmu_us_kal.KevinVoiceDirectory");
        final File[] files = Path.of("tts").toFile().listFiles();
        if (files != null) for (final File file : files) FileUtility.deleteFile(file.toPath(), true);

        // Register listeners
        jda.addEventListener(
                new GuildMemberListener(this),
                new GuildVoiceListener(this),
                new MessageListener(this));

        // Status log message
        LOGGER.info("Cobalt has finished starting!");
    }

    @Override
    public void setSettings() {
        settings
                .gatewayIntents(
                        GatewayIntent.SCHEDULED_EVENTS,
                        GatewayIntent.MESSAGE_CONTENT,
                        GatewayIntent.DIRECT_MESSAGES,
                        GatewayIntent.GUILD_PRESENCES,
                        GatewayIntent.GUILD_EXPRESSIONS,
                        GatewayIntent.GUILD_MEMBERS,
                        GatewayIntent.GUILD_MESSAGES,
                        GatewayIntent.GUILD_MESSAGE_TYPING,
                        GatewayIntent.GUILD_VOICE_STATES)
                .searchPaths(
                        "network.venox.cobalt.apps",
                        "network.venox.cobalt.commands",
                        "network.venox.cobalt.components")
                .embedDefault(LazyEmbed.Key.COLOR, 28864)
                .embedDefault(LazyEmbed.Key.FOOTER_TEXT, "Cobalt")
                .embedDefault(LazyEmbed.Key.FOOTER_ICON, "https://us-east-1.tixte.net/uploads/cdn.venox.network/zoomed.png");
    }

    @Override
    public void onNecessaryTasksDone() {
        config = new CoConfig(this);
        dataManager = new DataManager(this);
        
        config.loadStatuses();
        if (config.statuses != null) settings.activities(config.statuses);
    }

    public static void main(@NotNull String[] arguments) {
        new Cobalt();
    }
}

package network.venox.cobalt;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.requests.GatewayIntent;

import network.venox.cobalt.listeners.*;
import network.venox.cobalt.mongo.*;

import org.jetbrains.annotations.NotNull;

import xyz.srnyx.javautilities.FileUtility;

import xyz.srnyx.lazylibrary.LazyEmbed;
import xyz.srnyx.lazylibrary.LazyLibrary;

import xyz.srnyx.magicmongo.MagicDatabase;
import xyz.srnyx.magicmongo.SingleMongo;

import java.io.File;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;


public class Cobalt extends LazyLibrary {
    public CoConfig config;
    public MagicDatabase mongo;
    /**
    /**
     * Total member count across all guilds
     */
    public int totalMembers = 0;
    /**
     * Total unique members across all guilds
     */
    public int uniqueMembers;

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
                        "network.venox.cobalt.commands")
                .embedDefault(LazyEmbed.Key.COLOR, 28864)
                .embedDefault(LazyEmbed.Key.FOOTER_TEXT, "Cobalt")
                .embedDefault(LazyEmbed.Key.FOOTER_ICON, "https://us-east-1.tixte.net/uploads/cdn.venox.network/zoomed.png");
    }

    @Override
    public void onReady() {
        // mongo
        final String url = settings.fileSettings.file.yaml.node("mongo").getString();
        if (url == null) throw new IllegalArgumentException("MongoDB URL not found in config!");
        mongo = new SingleMongo(url).database.loadMagicCollections(Map.of(
                "auto_slowmodes", AutoSlowmode.class,
                "auto_threads", AutoThread.class,
                "users", CoUser.class,
                "limited_messages", LimitedMessages.class,
                "locks", Lock.class,
                "react_channels", ReactChannel.class,
                "servers", Server.class,
                "sticky_messages", StickyMessage.class));

        // guildCount, totalMembers, uniqueMembers
        final Set<Long> users = new HashSet<>();
        for (final Guild guild : jda.getGuilds()) guild.loadMembers().onSuccess(members -> {
            for (final Member member : members) {
                totalMembers++;
                users.add(member.getIdLong());
            }
        });
        uniqueMembers = users.size();

        // config
        config = new CoConfig(this);
    }

    public static void main(@NotNull String[] arguments) {
        new Cobalt();
    }
}

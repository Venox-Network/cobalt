package network.venox.cobalt;

import net.dv8tion.jda.api.entities.Activity;
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
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;


public class Cobalt extends LazyLibrary {
    public CoConfig config;
    public MagicDatabase mongo;

    public Cobalt() {
        // TTS
        System.setProperty("freetts.voices", "com.sun.speech.freetts.en.us.cmu_us_kal.KevinVoiceDirectory");
        final File[] files = Path.of("tts").toFile().listFiles();
        if (files != null) for (final File file : files) FileUtility.deleteFile(file.toPath(), true);

        // Register listeners
        jda.addEventListener(
                new ChannelListener(this),
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
                "corners", Corner.class,
                "corner_creators", CornerCreator.class,
                "users", CoUser.class,
                "limited_messages", LimitedMessages.class,
                "locks", Lock.class,
                "react_channels", ReactChannel.class,
                "servers", Server.class,
                "sticky_messages", StickyMessage.class));

        // config
        config = new CoConfig(this);

        // Load members for statuses
        final AtomicInteger totalMembers = new AtomicInteger();
        final Set<Long> users = ConcurrentHashMap.newKeySet();
        final List<Guild> guilds = jda.getGuilds();
        final List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (final Guild guild : guilds) {
            final CompletableFuture<Void> future = new CompletableFuture<>();
            guild.loadMembers()
                    .onError(error -> {
                        LOGGER.warn("Failed to load members for guild {} ({})", guild.getName(), guild.getIdLong(), error);
                        future.complete(null);
                    })
                    .onSuccess(members -> {
                        for (final Member member : members) {
                            if (member.getUser().isBot()) continue;
                            totalMembers.incrementAndGet();
                            users.add(member.getIdLong());
                        }
                        future.complete(null);
                    });
            futures.add(future);
        }
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .whenComplete((v, t) -> loadStatuses(guilds.size(), totalMembers, users.size()));
    }

    private void loadStatuses(int guilds, @NotNull AtomicInteger totalMembers, int uniqueMembers) {
        final String guildsString = String.valueOf(guilds);
        final String totalMembersString = String.valueOf(totalMembers.get());
        final String uniqueMembersString = String.valueOf(uniqueMembers);
        settings.activities(settings.fileSettings.file.yaml.node("statuses").childrenList().stream()
                .map(node -> {
                    // Get status
                    final String status = node.getString();
                    if (status == null) return null;

                    // Get type
                    Activity.ActivityType type = Activity.ActivityType.CUSTOM_STATUS;
                    if (status.startsWith("watching")) {
                        type = Activity.ActivityType.WATCHING;
                    } else if (status.startsWith("listening")) {
                        type = Activity.ActivityType.LISTENING;
                    } else if (status.startsWith("playing")) {
                        type = Activity.ActivityType.PLAYING;
                    }

                    // Get Activity
                    return Activity.of(type, status
                            .replace("%servers%", guildsString)
                            .replace("%users%", totalMembersString)
                            .replace("%unique_users%", uniqueMembersString));
                })
                .filter(Objects::nonNull)
                .toList());
    }

    public static void main(@NotNull String[] arguments) {
        new Cobalt();
    }
}

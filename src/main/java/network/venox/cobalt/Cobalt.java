package network.venox.cobalt;

import io.github.freya022.botcommands.api.core.annotations.BEventListener;
import io.github.freya022.botcommands.api.core.service.annotations.BService;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.requests.GatewayIntent;

import network.venox.cobalt.listeners.*;
import network.venox.cobalt.mongo.*;

import org.jetbrains.annotations.NotNull;

import xyz.srnyx.javautilities.FileUtility;

import xyz.srnyx.lazylibrary.LazyEmbed;
import xyz.srnyx.lazylibrary.LazyLibrary;
import xyz.srnyx.lazylibrary.services.Bot;

import java.io.File;
import java.nio.file.Path;
import java.util.*;


@BService
public class Cobalt implements Bot {
    public JDA jda;

    public Cobalt() {
        // TTS
        System.setProperty("freetts.voices", "com.sun.speech.freetts.en.us.cmu_us_kal.KevinVoiceDirectory");
        final File[] files = Path.of("tts").toFile().listFiles();
        if (files != null) for (final File file : files) FileUtility.deleteFile(file.toPath(), true);
    }

    @Override @NotNull
    public JDA getJDA() {
        return jda;
    }

    @BEventListener
    public void onReady(@NotNull ReadyEvent event, @NotNull LazyLibrary library) {
        jda = event.getJDA();

        // Load statuses
        int users = 0;
        final List<Guild> guilds = event.getJDA().getGuilds();
        for (final Guild guild : guilds) users += guild.getMemberCount();
        final String guildsString = String.valueOf(guilds.size());
        final String usersString = String.valueOf(users);
        library.activities(library.fileSettings.file.yaml.node("statuses").childrenList().stream()
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
                            .replace("%users%", usersString));
                })
                .filter(Objects::nonNull)
                .toList());
    }

    public static void main(@NotNull String[] arguments) {
        LazyLibrary.INSTANCE
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
                .searchPaths("network.venox.cobalt")
                .activities(new ArrayList<>())
                .embedDefault(LazyEmbed.Key.COLOR, 28864)
                .embedDefault(LazyEmbed.Key.FOOTER_TEXT, "Cobalt")
                .embedDefault(LazyEmbed.Key.FOOTER_ICON, "https://us-east-1.tixte.net/uploads/cdn.venox.network/zoomed.png")
                .build(Cobalt.class);
    }
}

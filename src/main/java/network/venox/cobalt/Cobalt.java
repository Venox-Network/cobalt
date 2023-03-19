package network.venox.cobalt;

import com.freya02.botcommands.api.CommandsBuilder;
import com.freya02.botcommands.api.components.DefaultComponentManager;

import com.zaxxer.hikari.HikariDataSource;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.managers.Presence;
import net.dv8tion.jda.api.managers.channel.concrete.VoiceChannelManager;
import net.dv8tion.jda.api.requests.GatewayIntent;

import network.venox.cobalt.commands.global.EmbedCmd;
import network.venox.cobalt.data.CoData;
import network.venox.cobalt.data.CoGuild;
import network.venox.cobalt.data.objects.CoStatsChannel;
import network.venox.cobalt.listeners.*;
import network.venox.cobalt.utility.CoUtilities;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.spongepowered.configurate.yaml.NodeStyle;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;


public class Cobalt {
    @NotNull public static final Logger LOGGER = LoggerFactory.getLogger("Cobalt");

    public JDA jda;
    @NotNull public final CoConfig config = new CoConfig();
    @NotNull public final CoFile messages = new CoFile("messages", NodeStyle.BLOCK, true);
    public CoData data;
    @NotNull public final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(10);
    @NotNull private final Random random = new Random();
    @Nullable public ScheduledFuture<?> statusTask;
    @NotNull public final Map<Long, CoGuildStats> guildStats = new HashMap<>();
    @NotNull public final Map<Long, EmbedCmd.Data> embedBuilders = new HashMap<>();

    public Cobalt() {
        // Start bot
        try {
            jda = JDABuilder.create(config.token,
                            GatewayIntent.SCHEDULED_EVENTS,
                            GatewayIntent.MESSAGE_CONTENT,
                            GatewayIntent.DIRECT_MESSAGES,
                            GatewayIntent.GUILD_PRESENCES,
                            GatewayIntent.GUILD_EMOJIS_AND_STICKERS,
                            GatewayIntent.GUILD_MEMBERS,
                            GatewayIntent.GUILD_MESSAGES,
                            GatewayIntent.GUILD_MESSAGE_TYPING,
                            GatewayIntent.GUILD_VOICE_STATES)
                    .build().awaitReady();
        } catch (final InterruptedException | IllegalArgumentException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
            System.exit(0);
            return;
        }

        // TTS
        System.setProperty("freetts.voices", "com.sun.speech.freetts.en.us.cmu_us_kal.KevinVoiceDirectory");
        final Path ttsPath = Path.of("tts");
        final File[] files = ttsPath.toFile().listFiles();
        if (files != null) for (final File file : files) CoUtilities.deleteFile(file.toPath());
        CoUtilities.deleteFile(ttsPath, true);

        // Load data
        data = new CoData(this);

        // Run Member loop
        for (final Guild guild : jda.getGuilds()) {
            final CoGuild coGuild = data.getGuild(guild);
            guild.loadMembers().onSuccess(members -> {
                guildStats.put(guild.getIdLong(), new CoGuildStats(members.size(), (int) members.stream()
                        .filter(member -> !member.getUser().isBot())
                        .count()));
                members.forEach(member -> coGuild.checkMemberNickname(member, coGuild.nicknameBlacklist));
            });
        }

        // Load config stuff that needs JDA
        config.loadJda(this);

        // Database
        //noinspection resource
        final HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(config.database);
        dataSource.setMaximumPoolSize(3);
        dataSource.setLeakDetectionThreshold(5000);

        // Register commands
        try {
            CommandsBuilder.newBuilder(config.owners.get(0))
                    .addOwners(config.owners.stream()
                            .mapToLong(Long::longValue)
                            .toArray())
                    .extensionsBuilder(extensions -> extensions.registerCommandDependency(Cobalt.class, () -> this))
                    .textCommandBuilder(textCommands -> textCommands.disableHelpCommand(true))
                    .setComponentManager(new DefaultComponentManager(() -> {
                        try {
                            return dataSource.getConnection();
                        } catch (final SQLException e) {
                            e.printStackTrace();
                            return null;
                        }
                    }))
                    .addSearchPath("network.venox.cobalt.commands")
                    .addSearchPath("network.venox.cobalt.apps")
                    .build(jda);
        } catch (final IOException e) {
            e.printStackTrace();
        }

        // Register listeners
        jda.addEventListener(
                new GuildMemberListener(this),
                new GuildMemberUpdateListener(this),
                new GuildVoiceListener(this),
                new MessageListener(this),
                new UserListener(this));

        // Presence (status)
        if (data.global.activity != null) {
            jda.getPresence().setActivity(data.global.activity);
        } else {
            startStatuses();
        }

        // Auto-save & statistic channels
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            data.save();
            for (CoGuild coGuild : data.guilds) {
                final Set<CoStatsChannel> statsChannels = coGuild.statsChannels;
                if (statsChannels.isEmpty()) continue;
                final Guild guild = coGuild.getGuild();
                if (guild != null) for (CoStatsChannel channel : statsChannels) {
                    final VoiceChannelManager manager = channel.update(guild);
                    if (manager != null) manager.queue();
                }
            }
        }, 10, 10, TimeUnit.MINUTES);

        // QOTD
        data.global.startQotd();

        // stop command
        new Thread(() -> {
            final Scanner scanner = new Scanner(System.in);
            while (scanner.hasNextLine()) {
                if (!scanner.nextLine().equals("stop")) continue;
                embedBuilders.values().forEach(builder -> {
                    final InteractionHook hook = builder.parameterHook;
                    if (hook != null) hook.deleteOriginal().complete();
                });
                data.save();
                System.exit(0);
            }
        }).start();
    }

    public void startStatuses() {
        final Presence presence = jda.getPresence();
        final Activity[] statuses = config.statuses.toArray(new Activity[0]);
        final int length = statuses.length;
        statusTask = scheduledExecutorService.scheduleAtFixedRate(() -> presence.setActivity(statuses[random.nextInt(length)]), 0, 30, TimeUnit.SECONDS);
    }

    @Contract(pure = true)
    public static void main(@NotNull String[] arguments) {
        new Cobalt();
    }
}

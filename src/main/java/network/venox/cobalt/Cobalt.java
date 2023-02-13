package network.venox.cobalt;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.managers.Presence;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;

import network.venox.cobalt.command.CoCommand;
import network.venox.cobalt.command.CoExecutableCommand;
import network.venox.cobalt.command.CoParentCommand;
import network.venox.cobalt.commands.*;
import network.venox.cobalt.commands.subcommands.*;
import network.venox.cobalt.data.CoData;
import network.venox.cobalt.listeners.*;
import network.venox.cobalt.managers.QotdManager;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import org.spongepowered.configurate.yaml.NodeStyle;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;


public class Cobalt {
    @NotNull public static final Logger logger = Logger.getLogger("Co");

    public JDA jda;
    @NotNull public final CoConfig config = new CoConfig();
    @NotNull public final CoFile messages = new CoFile("messages", NodeStyle.BLOCK, true);
    public CoData data;
    @NotNull public final List<CoCommand> commands = new ArrayList<>();
    @NotNull public final List<CoExecutableCommand> executableCommands = new ArrayList<>();
    @NotNull public final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(10);
    @Nullable public ScheduledFuture<?> statusTask;

    public Cobalt() {
        // Start bot
        try {
            jda = JDABuilder.create(config.token,
                            GatewayIntent.SCHEDULED_EVENTS,
                            GatewayIntent.MESSAGE_CONTENT,
                            GatewayIntent.GUILD_PRESENCES,
                            GatewayIntent.GUILD_EMOJIS_AND_STICKERS,
                            GatewayIntent.GUILD_MEMBERS,
                            GatewayIntent.GUILD_MESSAGES,
                            GatewayIntent.GUILD_MESSAGE_TYPING)
                    .setEnableShutdownHook(false)
                    .build().awaitReady();
        } catch (final InterruptedException | IllegalArgumentException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
            System.exit(0);
            return;
        }

        // Initialize variables
        config.loadJda(jda);
        data = new CoData(jda);

        // Register commands
        final CommandListUpdateAction update = jda.updateCommands();
        for (final CoCommand command : new CoCommand[]{
                new HighlightCmd(this),
                new NicknameCmd(this),
                new QotdCmd(this),
                new ReactCmd(this),
                new SuperCmd(this),
                new WarnCmd(this),
                new HelpCmd(this),
                new InviteCmd(this),
                new LeaveCmd(this),
                new ReloadCmd(this),
                new SayCmd(this),
                new ServersCmd(this),
                new SlowmodeCmd(this),
                new StatusCmd(this),
                new StickyCmd(this),
                new SupportCmd(this),
                new ThreadCmd(this)}) {
            // Register command
            //noinspection ResultOfMethodCallIgnored
            update.addCommands(command.toSlashCommandData());

            // Add to commands
            commands.add(command);

            // Add sub-commands to commands
            if (command instanceof CoParentCommand parent) {
                commands.addAll(parent.subCommands());
                executableCommands.addAll(parent.subCommands());
                continue;
            }

            // Add to executableCommands
            if (command instanceof CoExecutableCommand executable) executableCommands.add(executable);
        }
        update.queue();

        // Register listeners
        jda.addEventListener(
                new GuildMemberListener(this),
                new GuildMemberUpdateListener(this),
                new InteractionListener(this),
                new MessageListener(this));

        // Status(es)
        if (data.global.activity != null) {
            jda.getPresence().setActivity(data.global.activity);
        } else {
            startStatuses();
        }

        // Auto-save
        scheduledExecutorService.scheduleAtFixedRate(() -> data.save(), 10, 10, TimeUnit.MINUTES);

        // QOTD
        new QotdManager(this).start();

        // stop command
        new Thread(() -> {
            final Scanner scanner = new Scanner(System.in);
            while (scanner.hasNextLine()) {
                if (!scanner.nextLine().equals("stop")) continue;
                data.save();
                System.exit(0);
            }
        }).start();
    }

    public void startStatuses() {
        final Presence presence = jda.getPresence();
        final Activity[] statuses = config.statuses.toArray(new Activity[0]);
        final Random random = new Random();
        final int length = statuses.length;
        statusTask = scheduledExecutorService.scheduleAtFixedRate(() -> presence.setActivity(statuses[random.nextInt(length)]), 0, 20, TimeUnit.SECONDS);
    }

    @Contract(pure = true)
    public static void main(@NotNull String[] arguments) {
        new Cobalt();
    }
}

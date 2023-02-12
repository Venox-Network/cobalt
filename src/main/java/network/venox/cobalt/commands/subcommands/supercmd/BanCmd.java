package network.venox.cobalt.commands.subcommands.supercmd;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import network.venox.cobalt.command.CoSubCommand;
import network.venox.cobalt.commands.subcommands.SuperCmd;
import network.venox.cobalt.data.objects.CoSuperBan;
import network.venox.cobalt.data.objects.CoEmbed;
import network.venox.cobalt.events.CoSlashCommandInteractionEvent;
import network.venox.cobalt.utility.CoMapper;
import network.venox.cobalt.utility.DurationParser;

import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;


public class BanCmd extends CoSubCommand {
    public BanCmd(@NotNull SuperCmd parent) {
        super(parent);
    }

    @Override @NotNull
    public String description() {
        return "Bans the specified user from all Venox servers";
    }

    @Override @NotNull
    public List<OptionData> options() {
        return Arrays.asList(
                new OptionData(OptionType.STRING, "user", "The ID of the user to ban", true),
                new OptionData(OptionType.STRING, "reason", "The reason for the ban", true),
                new OptionData(OptionType.STRING, "duration", "Duration of the ban. If empty, ban is permanent", false));
    }

    @Override
    public void onCommand(@NotNull CoSlashCommandInteractionEvent event) {
        final OptionMapping userOption = event.getOption("user");
        final OptionMapping reasonOption = event.getOption("reason");
        if (userOption == null || reasonOption == null) return;
        final OptionMapping durationOption = event.getOption("duration");
        final JDA jda = event.getJDA();

        // user
        final long userId = userOption.getAsLong();
        final User user = CoMapper.handleException(() -> jda.retrieveUserById(userId).complete());
        if (user == null) {
            event.replyEmbeds(CoEmbed.invalidArgument(userId).build()).setEphemeral(true).queue();
            return;
        }

        // Check if user is already banned
        final CoSuperBan current = cobalt.data.global.superBans.stream()
                .filter(ban -> ban.user() == userId)
                .findFirst()
                .orElse(null);
        if (current != null) {
            if (!current.isExpired()) {
                final User currentMod = current.getModerator(jda);
                if (currentMod != null) event.replyEmbeds(cobalt.messages.getEmbed("command", "super", "ban", "already")
                                .replace("%username%", user.getName())
                                .replace("%mention%", user.getAsMention())
                                .replace("%reason%", current.reason())
                                .replace("%timeleft%", current.getTimeLeft())
                                .replace("%moderator%", currentMod.getAsMention())
                                .build())
                        .setEphemeral(true).queue();
                return;
            }
            cobalt.data.global.superBans.remove(current);
        }

        // duration
        Long duration = null;
        final String durationString = durationOption == null ? "Permanent" : durationOption.getAsString();
        if (durationOption != null) {
            final Duration newDuration = DurationParser.parse(durationString);
            if (newDuration == null) {
                event.replyEmbeds(CoEmbed.invalidArgument(durationString).build()).setEphemeral(true).queue();
                return;
            }
            duration = System.currentTimeMillis() + newDuration.toMillis();
        }

        // Add ban
        final User moderator = event.getUser();
        final CoSuperBan superBan = new CoSuperBan(userId, reasonOption.getAsString(), duration, moderator.getIdLong());
        cobalt.data.global.superBans.add(superBan);

        // Send embed to moderator
        event.replyEmbeds(cobalt.messages.getEmbed("command", "super", "ban", "success")
                        .replace("%username%", user.getName())
                        .replace("%mention%", user.getAsMention())
                        .replace("%reason%", superBan.reason())
                        .replace("%duration%", durationString)
                        .build())
                .setEphemeral(true).queue();

        // Send embed to user
        user.openPrivateChannel()
                .flatMap(channel -> channel.sendMessageEmbeds(cobalt.messages.getEmbed("command", "super", "ban", "user")
                        .replace("%reason%", superBan.reason())
                        .replace("%duration%", durationString)
                        .replace("%moderator%", moderator.getAsMention())
                        .build()))
                .complete();

        // Send log
        cobalt.config.sendLog("superban", "**User:** " + user.getAsMention() + "\n**Reason:** " + superBan.reason() + "\n**Duration:** " + durationString + "\n**Moderator:** " + moderator.getAsMention());

        // Ban user
        superBan.ban(jda);
    }
}

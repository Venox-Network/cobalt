package network.venox.cobalt.commands.global;

import com.freya02.botcommands.api.annotations.CommandMarker;
import com.freya02.botcommands.api.annotations.Dependency;
import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.CommandScope;
import com.freya02.botcommands.api.application.annotations.AppOption;
import com.freya02.botcommands.api.application.slash.GlobalSlashEvent;
import com.freya02.botcommands.api.application.slash.annotations.JDASlashCommand;
import com.freya02.botcommands.api.application.slash.autocomplete.annotations.AutocompletionHandler;
import com.freya02.botcommands.api.components.Components;
import com.freya02.botcommands.api.components.event.ButtonEvent;
import com.freya02.botcommands.api.utils.ButtonContent;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;

import network.venox.cobalt.Cobalt;
import network.venox.cobalt.data.objects.CoSuperBan;
import network.venox.cobalt.CoUtilities;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.javautilities.manipulation.DurationParser;

import xyz.srnyx.lazylibrary.LazyEmbed;
import xyz.srnyx.lazylibrary.LazyEmoji;
import xyz.srnyx.lazylibrary.utility.LazyMapper;

import java.time.Duration;
import java.util.List;
import java.util.Optional;


@CommandMarker
public class SuperCmd extends ApplicationCommand {
    @NotNull private static final String AC_BAN_USER = "SuperCmd.banCommand.user";
    @NotNull private static final String AC_UNBAN_USER = "SuperCmd.unbanCommand.user";

    @Dependency private Cobalt bot;

    @JDASlashCommand(
            scope = CommandScope.GLOBAL,
            name = "super",
            subcommand = "ban",
            description = "Bans the specified user from all Venox servers")
    public void banCommand(@NotNull GlobalSlashEvent event,
                          @AppOption(description = "The ID of the user to ban", autocomplete = AC_BAN_USER) @NotNull String user,
                          @AppOption(description = "The reason for the ban") @NotNull String reason,
                          @AppOption(description = "Duration of the ban. If empty, ban is permanent") @Nullable String duration) {
        if (!bot.config.checkIsOwner(event)) return;
        if (user.equals(event.getJDA().getSelfUser().getId())) {
            event.reply(LazyEmoji.NO + " I can't ban myself!").setEphemeral(true).queue();
            return;
        }
        final UserSnowflake snowflake = CoUtilities.getUserSnowflake(bot, event, user);
        if (snowflake == null) return;

        // Check if user is already banned
        final CoSuperBan current = bot.oldData.global.getSuperBan(snowflake.getIdLong());
        if (current != null) {
            if (!current.isExpired()) {
                current.getUser()
                        .flatMap(userJda -> event.replyEmbeds(new LazyEmbed()
                                .setTitle(userJda.getName() + " already super-banned!")
                                .addField("User", userJda.getAsMention(), true)
                                .addField("Reason", current.reason, true)
                                .addField("Time left", current.getTimeLeft(), true)
                                .addField("Moderator", "<@" + current.getModerator() + ">", true)
                                .build(bot)).setEphemeral(true))
                        .queue();
                return;
            }
            bot.oldData.global.superBans.remove(current);
        }

        // duration
        Long durationLong = null;
        final String durationString = duration == null ? "Permanent" : duration;
        if (duration != null) {
            final Optional<Duration> newDuration = DurationParser.parse(durationString);
            if (newDuration.isEmpty()) {
                event.replyEmbeds(bot.embeds.invalidArgument(durationString)).setEphemeral(true).queue();
                return;
            }
            durationLong = System.currentTimeMillis() + newDuration.get().toMillis();
        }

        // Confirmation message
        final Long finalDurationLong = durationLong;
        event.reply("Are you sure you want to **superban** " + snowflake.getAsMention() + "?\nThis will ban them from **all** Venox Network servers!")
                .addActionRow(
                        Components.successButton(buttonEvent -> ban(buttonEvent, user, reason, finalDurationLong, durationString)).build(new ButtonContent("Yes", LazyEmoji.YES_CLEAR.getEmoji())),
                        Components.dangerButton(buttonEvent -> buttonEvent.editMessage(LazyEmoji.YES + " Cancelled!").setComponents(List.of()).queue()).build(new ButtonContent("No", LazyEmoji.NO_CLEAR_DARK.getEmoji())))
                .setEphemeral(true).queue();
    }

    @JDASlashCommand(
            scope = CommandScope.GLOBAL,
            name = "super",
            subcommand = "unban",
            description = "Unbans the specified user from all Venox servers")
    public void unbanCommand(@NotNull GlobalSlashEvent event,
                          @AppOption(description = "The ID of the user to unban", autocomplete = AC_UNBAN_USER) @NotNull String user) {
        if (!bot.config.checkIsOwner(event)) return;
        final UserSnowflake snowflake = LazyMapper.toUserSnowflake(user).orElse(null);
        if (snowflake == null) {
            event.replyEmbeds(LazyEmbed.invalidArgument("user", user).build(bot)).setEphemeral(true).queue();
            return;
        }

        // Get ban
        final CoSuperBan ban = bot.oldData.global.getSuperBan(snowflake.getIdLong());
        if (ban == null) {
            event.reply(LazyEmoji.NO + " " + snowflake.getAsMention() + " is not super-banned!").setEphemeral(true).queue();
            return;
        }

        // Unban user
        bot.oldData.global.superBans.remove(ban);
        ban.unban();

        // Reply
        event.reply(LazyEmoji.YES + " " + snowflake + " has been unbanned from all Venox servers").setEphemeral(true).queue();
    }

    @JDASlashCommand(
            scope = CommandScope.GLOBAL,
            name = "super",
            subcommand = "kick",
            description = "Kicks the specified user from all Venox servers")
    public void kickCommand(@NotNull GlobalSlashEvent event,
                          @AppOption(description = "The ID of the user to kick", autocomplete = AC_BAN_USER) @NotNull String user,
                          @AppOption(description = "The reason for the kick") @NotNull String reason) {
        if (!bot.config.checkIsOwner(event)) return;
        if (user.equals(event.getJDA().getSelfUser().getId())) {
            event.reply(LazyEmoji.NO + " I can't kick myself!").setEphemeral(true).queue();
            return;
        }
        final UserSnowflake snowflake = CoUtilities.getUserSnowflake(bot, event, user);
        if (snowflake == null) return;

        // Confirmation message
        event.reply(LazyEmoji.WARNING + " Are you sure you want to **superkick** " + snowflake.getAsMention() + "?\nThis will kick them from **all** Venox Network servers!")
                .addActionRow(
                        Components.successButton(buttonEvent -> kick(buttonEvent, user, reason)).build(new ButtonContent("Yes", LazyEmoji.YES_CLEAR.emoji)),
                        Components.dangerButton(buttonEvent -> buttonEvent.editMessage(LazyEmoji.YES + " Cancelled!").setComponents(List.of()).queue()).build(new ButtonContent("No", LazyEmoji.NO_CLEAR_DARK.emoji)))
                .setEphemeral(true).queue();
    }

    @AutocompletionHandler(name = AC_BAN_USER) @NotNull
    public List<Command.Choice> acBanUser(@NotNull CommandAutoCompleteInteractionEvent event) {
        if (!bot.isOwner(event.getUser().getIdLong())) return List.of();
        return CoUtilities.acGuildMembers(event);
    }

    @AutocompletionHandler(name = AC_UNBAN_USER) @NotNull
    public List<Command.Choice> acUnbanUser(@NotNull CommandAutoCompleteInteractionEvent event) {
        if (!bot.isOwner(event.getUser().getIdLong())) return List.of();
        return bot.oldData.global.superBans.stream()
                .map(ban -> new Command.Choice(ban.getUser().complete().getName(), ban.user))
                .toList();
    }

    private void ban(@NotNull ButtonEvent event, @NotNull String user, @NotNull String reason, @Nullable Long duration, @NotNull String durationString) {
        final JDA jda = event.getJDA();

        // Get User
        final User userJda = jda.getUserById(user);
        if (userJda == null) return;

        // Add ban
        final User moderator = event.getUser();
        final CoSuperBan superBan = new CoSuperBan(jda, userJda.getIdLong(), reason, duration, moderator.getIdLong());
        bot.oldData.global.superBans.add(superBan);

        // Send message to moderator
        event.editMessageEmbeds(new LazyEmbed()
                        .setTitle("Banned " + userJda.getName() + " from all Venox servers")
                        .addField("User", userJda.getAsMention(), true)
                        .addField("Reason", reason, true)
                        .addField("Duration", durationString, true)
                        .build(bot))
                .setContent("")
                .setComponents(List.of())
                .queue();

        // Send message to user
        userJda.openPrivateChannel()
                .flatMap(channel -> channel.sendMessageEmbeds(new LazyEmbed()
                        .setTitle("You've been super-banned!")
                        .setDescription("You have been super-banned from Venox Network, meaning you can't join any Venox Network servers.")
                        .addField("Reason", reason, true)
                        .addField("Duration", durationString, true)
                        .addField("Moderator", moderator.getAsMention(), true)
                        .build(bot)))
                .queue(s -> {}, f -> {});

        // Send log
        bot.config.guild.sendLog("superban", "**User:** " + userJda.getAsMention() + "\n**Reason:** " + reason + "\n**Duration:** " + durationString + "\n**Moderator:** " + moderator.getAsMention());

        // Ban user
        superBan.ban();
    }

    private void kick(@NotNull ButtonEvent event, @NotNull String user, @NotNull String reason) {
        final User userJda = event.getJDA().getUserById(user);
        if (userJda == null) return;

        // Send message to moderator
        event.editMessage(LazyEmoji.YES + " Kicked " + userJda.getAsMention() + " from **all** Venox Network servers")
                .setComponents(List.of())
                .queue();

        // Send message to user
        userJda.openPrivateChannel()
                .flatMap(channel -> channel.sendMessage(LazyEmoji.WARNING + " You have been kicked from **all** Venox Network servers by " + event.getUser().getAsMention() + " for the following reason:\n> " + reason))
                .queue(s -> {}, f -> {});

        // Kick user from all guilds
        for (final Guild guild : event.getJDA().getGuilds()) guild.kick(userJda).reason(reason).queue(s -> {}, f -> {});

        // Log
        bot.config.guild.sendLog("superkick", "**User:** " + userJda.getAsMention() + "\n**Reason:** " + reason + "\n**Moderator:** " + event.getUser().getAsMention());
    }
}

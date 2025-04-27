package network.venox.cobalt.commands.global;

import com.freya02.botcommands.api.annotations.CommandMarker;
import com.freya02.botcommands.api.annotations.Dependency;
import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.CommandScope;
import com.freya02.botcommands.api.application.annotations.AppOption;
import com.freya02.botcommands.api.application.slash.GlobalSlashEvent;
import com.freya02.botcommands.api.application.slash.annotations.JDASlashCommand;
import com.freya02.botcommands.api.application.slash.autocomplete.annotations.AutocompletionHandler;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.requests.restaction.CacheRestAction;

import network.venox.cobalt.Cobalt;
import network.venox.cobalt.data.objects.CoWarning;
import network.venox.cobalt.CoUtilities;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.javautilities.manipulation.Mapper;

import xyz.srnyx.lazylibrary.LazyEmoji;
import xyz.srnyx.lazylibrary.utility.LazyMapper;

import java.util.List;
import java.util.Objects;
import java.util.Optional;


@CommandMarker
public class WarnCmd extends ApplicationCommand {
    @NotNull private static final String AC_ADD_USER = "WarnCmd.addCommand.user";
    @NotNull private static final String AC_LIST_USER = "WarnCmd.listCommand.user";
    @NotNull private static final String AC_LIST_ID = "WarnCmd.listCommand.id";

    @Dependency private Cobalt cobalt;

    @JDASlashCommand(
            scope = CommandScope.GLOBAL,
            name = "warn",
            subcommand = "list",
            description = "List all the global warnings (of a user)")
    public void listCommand(@NotNull GlobalSlashEvent event,
                            @AppOption(description = "The user to list the warnings of", autocomplete = AC_LIST_USER) @Nullable String user,
                            @AppOption(description = "The ID of the warning to remove", autocomplete = AC_LIST_ID) @Nullable Integer id) {
        if (!cobalt.config.checkIsOwner(event)) return;

        // all
        if (user == null && id == null) {
            final List<CoWarning> warnings = cobalt.oldData.global.warnings;
            if (warnings.isEmpty()) {
                event.reply(LazyEmoji.NO + " No warnings found!").setEphemeral(true).queue();
                return;
            }

            // Reply
            event.reply(warningsToString(warnings, null)).setEphemeral(true).queue();
            return;
        }

        // id
        if (id != null) {
            final CoWarning warning = cobalt.oldData.global.getWarning(id);
            if (warning == null) {
                event.reply(LazyEmoji.NO + " No warning found with ID `" + id + "`!").setEphemeral(true).queue();
                return;
            }

            // Reply
            final Optional<CacheRestAction<User>> userAction = warning.getUser();
            final Optional<CacheRestAction<User>> moderatorAction = warning.getModerator();
            if (userAction.isEmpty() || moderatorAction.isEmpty()) return;
            userAction.get()
                    .flatMap(warningUser -> moderatorAction.get()
                            .flatMap(moderator -> event.reply("**ID:** " + id + "\n**User:** " + warningUser.getAsMention() + "\n**Reason:** " + warning.reason + "\n**Moderator:** " + moderator.getAsMention()).setEphemeral(true)))
                    .queue();
            return;
        }

        // user
        final UserSnowflake snowflake = CoUtilities.getUserSnowflake(cobalt, event, user);
        if (snowflake == null) return;
        final List<CoWarning> warnings = getWarnings(event, snowflake);
        if (warnings == null) return;

        // Reply
        event.reply(warningsToString(warnings, snowflake)).setEphemeral(true).queue();
    }

    @JDASlashCommand(
            scope = CommandScope.GLOBAL,
            name = "warn",
            subcommand = "add",
            description = "Adds a global warning to a user")
    public void addCommand(@NotNull GlobalSlashEvent event,
                           @AppOption(description = "The user to add a warning to", autocomplete = AC_ADD_USER) @NotNull String user,
                           @AppOption(description = "The reason for the warning") @NotNull String reason) {
        if (!cobalt.config.checkIsOwner(event)) return;
        final UserSnowflake snowflake = CoUtilities.getUserSnowflake(cobalt, event, user);
        if (snowflake == null) return;
        final JDA jda = event.getJDA();
        final long id = snowflake.getIdLong();
        final User moderator = event.getUser();

        // Add warning
        cobalt.oldData.global.warnings.add(new CoWarning(jda, cobalt.oldData.global.getNextWarningId(), id, reason, moderator.getIdLong()));
        final int count = cobalt.oldData.global.getWarnings(id).size();

        // Reply
        event.reply(LazyEmoji.YES + " **Warned " + snowflake.getAsMention() + " for:**\n> " + reason + "\nThey now have **" + count + "** global warning(s)!").setEphemeral(true).queue();

        // Log
        cobalt.config.guild.sendLog("add warning", "**User:** " + snowflake.getAsMention() + "\n**Reason:** " + reason + "\n**Moderator:** " + moderator.getAsMention() + "\n**Warnings:** " + count);

        // DM user
        jda.retrieveUserById(id)
                .flatMap(User::openPrivateChannel)
                .flatMap(channel -> channel.sendMessage(LazyEmoji.WARNING + " **You have been globally warned in Venox Network for:**\n> " + reason + "\nYou now have **" + count + "** global warning(s)!"))
                .queue();
    }

    @JDASlashCommand(
            scope = CommandScope.GLOBAL,
            name = "warn",
            subcommand = "remove",
            description = "Removes a global warning from a user")
    public void removeCommand(@NotNull GlobalSlashEvent event,
                              @AppOption(description = "The user to remove a warning from", autocomplete = AC_LIST_USER) @Nullable String user,
                              @AppOption(description = "The ID of the warning to remove", autocomplete = AC_LIST_ID) @Nullable Integer id) {
        if (!cobalt.config.checkIsOwner(event)) return;
        if (user == null && id == null) {
            event.reply(LazyEmoji.NO + " You must specify a user and/or a warning ID!").setEphemeral(true).queue();
            return;
        }
        final String moderator = event.getUser().getAsMention();

        // id
        if (id != null) {
            final CoWarning warning = cobalt.oldData.global.getWarning(id);
            if (warning == null) {
                event.reply(LazyEmoji.NO + " No warning found with ID `" + id + "`!").setEphemeral(true).queue();
                return;
            }

            // Remove warning
            cobalt.oldData.global.warnings.remove(warning);

            // Reply and log
            warning.getUser().ifPresent(userAction -> userAction
                    .queue(warningUser -> {
                        event.reply(LazyEmoji.YES + " Removed warning from **" + warningUser.getAsMention() + "**").setEphemeral(true).queue();
                        cobalt.config.guild.sendLog("remove warning", "**ID:** " + warning.id + "\n**User:** " + warningUser.getAsMention() + "\n**Reason:** " + warning.reason + "\n**Moderator:** " + moderator);
                    }));
            return;
        }

        // user
        final UserSnowflake snowflake = CoUtilities.getUserSnowflake(cobalt, event, user);
        if (snowflake == null) return;
        final List<CoWarning> warnings = getWarnings(event, snowflake);
        if (warnings == null) return;

        // Remove warnings
        cobalt.oldData.global.warnings.removeAll(warnings);

        // Reply and log
        event.reply(LazyEmoji.YES + " Removed **" + warnings.size() + "** warning(s) from " + snowflake.getAsMention()).setEphemeral(true).queue();
        cobalt.config.guild.sendLog("remove warnings", "**User:** " + snowflake.getAsMention() + "\n**Warnings:** " + warnings.size() + "\n**Moderator:** " + moderator);
    }

    @AutocompletionHandler(name = AC_LIST_USER) @NotNull
    public List<Command.Choice> acListUser(@NotNull CommandAutoCompleteInteractionEvent event) {
        if (!cobalt.isOwner(event.getUser().getIdLong())) return List.of();
        return cobalt.oldData.global.warnings.stream()
                .map(warning -> {
                    final CacheRestAction<User> userAction = warning.getUser();
                    if (userAction == null) return null;
                    return new Command.Choice(userAction.complete().getAsTag(), warning.user);
                })
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    @AutocompletionHandler(name = AC_LIST_ID) @NotNull
    public List<String> acListId(@NotNull CommandAutoCompleteInteractionEvent event,
                                 @AppOption @Nullable String user) {
        if (!cobalt.isOwner(event.getUser().getIdLong())) return List.of();
        final Optional<Long> userId = Mapper.toLong(user);
        List<CoWarning> warnings = cobalt.oldData.global.warnings;
        if (userId.isPresent()) warnings = cobalt.oldData.global.getWarnings(userId.get());
        return warnings.stream()
                .map(warning -> String.valueOf(warning.id))
                .toList();
    }

    @AutocompletionHandler(name = AC_ADD_USER) @NotNull
    public List<Command.Choice> acAddUser(@NotNull CommandAutoCompleteInteractionEvent event) {
        if (!cobalt.isOwner(event.getUser().getIdLong())) return List.of();
        return CoUtilities.acGuildMembers(event);
    }

    @Nullable
    private List<CoWarning> getWarnings(@NotNull GlobalSlashEvent event, @NotNull UserSnowflake snowflake) {
        final List<CoWarning> warnings = cobalt.oldData.global.getWarnings(snowflake.getIdLong());
        if (warnings.isEmpty()) {
            event.reply(LazyEmoji.NO + " No warnings found!").setEphemeral(true).queue();
            return null;
        }
        return warnings;
    }

    @NotNull
    private String warningsToString(@NotNull List<CoWarning> warnings, @Nullable UserSnowflake snowflake) {
        final StringBuilder builder = new StringBuilder();
        for (final CoWarning warning : warnings) {
            final UserSnowflake warningUser = snowflake != null ? snowflake : LazyMapper.toUserSnowflake(warning.user).orElse(null);
            final Optional<UserSnowflake> moderator = LazyMapper.toUserSnowflake(warning.moderator);
            if (warningUser != null && moderator.isPresent()) builder
                    .append("**ID:** ").append(warning.id)
                    .append("\n**User:** ").append(warningUser.getAsMention())
                    .append("\n**Reason:** ").append(warning.reason)
                    .append("\n**Moderator:** ").append(moderator.get().getAsMention())
                    .append("\n\n");
        }
        return builder.toString();
    }
}

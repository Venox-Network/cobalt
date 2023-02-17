package network.venox.cobalt.commands;

import com.freya02.botcommands.api.annotations.CommandMarker;
import com.freya02.botcommands.api.annotations.Dependency;
import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.CommandScope;
import com.freya02.botcommands.api.application.annotations.AppOption;
import com.freya02.botcommands.api.application.slash.GlobalSlashEvent;
import com.freya02.botcommands.api.application.slash.annotations.JDASlashCommand;
import com.freya02.botcommands.api.application.slash.autocomplete.annotations.AutocompletionHandler;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;

import network.venox.cobalt.Cobalt;
import network.venox.cobalt.data.objects.CoWarning;
import network.venox.cobalt.utility.CoMapper;
import network.venox.cobalt.utility.CoUtilities;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;


@CommandMarker
public class WarnCmd extends ApplicationCommand {
    @NotNull private static final String AC_ADD_USER = "WarnCmd.addCommand.user";
    @NotNull private static final String AC_LIST_USER = "WarnCmd.listCommand.user";
    @NotNull private static final String AC_LIST_ID = "WarnCmd.listCommand.id";

    @Dependency private Cobalt cobalt;

    @JDASlashCommand(
            scope = CommandScope.GLOBAL,
            name = "warn",
            subcommand = "add",
            description = "Adds a global warning to a user")
    public void addCommand(@NotNull GlobalSlashEvent event,
                          @AppOption(description = "The user to add a warning to", autocomplete = AC_ADD_USER) @NotNull String user,
                          @AppOption(description = "The reason for the warning") @NotNull String reason) {
        if (!cobalt.config.checkIsOwner(event)) return;
        final User userJda = CoUtilities.getUser(event, user);
        if (userJda == null) return;
        final User moderator = event.getUser();

        // Add warning
        cobalt.data.global.warnings.add(new CoWarning(event.getJDA(), cobalt.data.global.getNextWarningId(), userJda.getIdLong(), reason, moderator.getIdLong()));
        final int count = cobalt.data.global.getWarnings(userJda.getIdLong()).size();

        // Reply
        event.reply("**Warned " + userJda.getAsMention() + " for:**\n> " + reason + "\nThey now have **" + count + "** global warning(s)!").setEphemeral(true).queue();

        // DM user
        userJda.openPrivateChannel()
                .flatMap(channel -> channel.sendMessage("**You have been globally warned in Venox Network for:**\n> " + reason + "\nYou now have **" + count + "** global warning(s)!"))
                .queue();

        // Log
        cobalt.config.sendLog("add warning", "**User:**" + userJda.getAsMention() + "\n**Reason:**" + reason + "\n**Moderator:**" + moderator.getAsMention() + "\n**Warnings:**" + count);
    }

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
            final List<CoWarning> warnings = cobalt.data.global.warnings;
            if (warnings.isEmpty()) {
                event.reply("No warnings found").setEphemeral(true).queue();
                return;
            }

            // Reply
            event.reply(warningsToString(warnings, null)).setEphemeral(true).queue();
            return;
        }

        // id
        if (id != null) {
            final CoWarning warning = cobalt.data.global.getWarning(id);
            if (warning == null) {
                event.reply("No warning found with ID `" + id + "`").setEphemeral(true).queue();
                return;
            }

            // Reply
            final User warningUser = warning.getUser();
            final User moderator = warning.getModerator();
            if (warningUser != null && moderator != null) event.reply("**ID:** " + id + "\n**User:** " + warningUser.getAsMention() + "\n**Reason:** " + warning.reason() + "\n**Moderator:** " + moderator.getAsMention()).setEphemeral(true).queue();
            return;
        }

        // user
        final User userJda = CoUtilities.getUser(event, user);
        if (userJda == null) return;
        final List<CoWarning> warnings = getWarnings(event, userJda);
        if (warnings == null) return;

        // Reply
        event.reply(warningsToString(warnings, userJda)).setEphemeral(true).queue();
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
            event.reply("You must specify a user and/or a warning ID").setEphemeral(true).queue();
            return;
        }
        final String moderator = event.getUser().getAsMention();

        // id
        if (id != null) {
            final CoWarning warning = cobalt.data.global.getWarning(id);
            if (warning == null) {
                event.reply("No warning found with ID `" + id + "`").setEphemeral(true).queue();
                return;
            }

            // Remove warning
            cobalt.data.global.warnings.remove(warning);

            // Reply and log
            final User warningUser = warning.getUser();
            if (warningUser == null) return;
            event.reply("Removed warning from **" + warningUser.getAsMention() + "**").setEphemeral(true).queue();
            cobalt.config.sendLog("remove warning", "**ID:** " + warning.id() + "\n**User:** " + warningUser.getAsMention() + "\n**Reason:** " + warning.reason() + "\n**Moderator:** " + moderator);
            return;
        }

        // user
        final User userJda = CoUtilities.getUser(event, user);
        if (userJda == null) return;
        final List<CoWarning> warnings = getWarnings(event, userJda);
        if (warnings == null) return;

        // Remove warnings
        cobalt.data.global.warnings.removeAll(warnings);

        // Reply and log
        event.reply("Removed **" + warnings.size() + "** warning(s) from **" + userJda.getAsTag() + "**").setEphemeral(true).queue();
        cobalt.config.sendLog("remove warnings", "**User:** " + userJda.getAsMention() + "\n**Warnings:** " + warnings.size() + "\n**Moderator:** " + moderator);
    }

    @AutocompletionHandler(name = AC_ADD_USER) @NotNull
    public List<Command.Choice> addUserAutoComplete(@NotNull CommandAutoCompleteInteractionEvent event) {
        if (!cobalt.config.isOwner(event.getUser())) return List.of();
        return CoUtilities.acGuildMembers(event);
    }

    @AutocompletionHandler(name = AC_LIST_USER) @NotNull
    public List<Command.Choice> listRemoveUserAutoComplete(@NotNull CommandAutoCompleteInteractionEvent event) {
        if (!cobalt.config.isOwner(event.getUser())) return List.of();
        return cobalt.data.global.warnings.stream()
                .map(warning -> {
                    final User user = warning.getUser();
                    if (user == null) return null;
                    return new Command.Choice(user.getAsTag(), user.getIdLong());
                })
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    @AutocompletionHandler(name = AC_LIST_ID) @NotNull
    public List<String> listRemoveIdAutoComplete(@NotNull CommandAutoCompleteInteractionEvent event,
                                                @AppOption @Nullable String user) {
        if (!cobalt.config.isOwner(event.getUser())) return List.of();
        final Long userId = CoMapper.toLong(user);
        List<CoWarning> warnings = cobalt.data.global.warnings;
        if (userId != null) warnings = cobalt.data.global.getWarnings(userId);
        return warnings.stream()
                .map(warning -> String.valueOf(warning.id()))
                .toList();
    }

    @Nullable
    private List<CoWarning> getWarnings(@NotNull GlobalSlashEvent event, @NotNull User user) {
        final List<CoWarning> warnings = cobalt.data.global.getWarnings(user.getIdLong());
        if (warnings.isEmpty()) {
            event.reply("No warnings found").setEphemeral(true).queue();
            return null;
        }
        return warnings;
    }

    @NotNull
    private String warningsToString(@NotNull List<CoWarning> warnings, @Nullable User user) {
        final StringBuilder builder = new StringBuilder();
        for (final CoWarning warning : warnings) {
            final User warningUser = user == null ? warning.getUser() : user;
            final User moderator = warning.getModerator();
            if (warningUser != null && moderator != null) builder
                    .append("**ID:** ").append(warning.id())
                    .append("\n**User:** ").append(warningUser.getAsMention())
                    .append("\n**Reason:** ").append(warning.reason())
                    .append("\n**Moderator:** ").append(moderator.getAsMention())
                    .append("\n\n");
        }
        return builder.toString();
    }
}

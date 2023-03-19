package network.venox.cobalt.commands.guild;

import com.freya02.botcommands.api.annotations.CommandMarker;
import com.freya02.botcommands.api.annotations.Dependency;
import com.freya02.botcommands.api.annotations.UserPermissions;
import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.CommandScope;
import com.freya02.botcommands.api.application.annotations.AppOption;
import com.freya02.botcommands.api.application.slash.GuildSlashEvent;
import com.freya02.botcommands.api.application.slash.annotations.JDASlashCommand;
import com.freya02.botcommands.api.application.slash.autocomplete.annotations.AutocompletionHandler;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import network.venox.cobalt.Cobalt;
import network.venox.cobalt.data.CoGuild;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;


@CommandMarker @UserPermissions(Permission.MANAGE_ROLES)
public class StatusRoleCmd extends ApplicationCommand {
    @NotNull private static final String AC_REMOVE_STATUS = "StatusRoleCmd.removeCommand.status";

    @Dependency private Cobalt cobalt;

    @JDASlashCommand(
            scope = CommandScope.GUILD,
            name = "statusrole",
            subcommand = "list",
            description = "List all roles that will be given to users who have specific status")
    public void listCommand(@NotNull GuildSlashEvent event,
                            @AppOption(description = "The role to list the statuses of") @Nullable Role role) {
        final Guild guild = event.getGuild();
        final CoGuild coGuild = cobalt.data.getGuild(guild);

        // All status roles
        if (role == null) {
            final StringBuilder builder = new StringBuilder();
            for (Map.Entry<Long, Set<String>> entry : coGuild.statusRoles.entrySet()) {
                final Role r = guild.getRoleById(entry.getKey());
                if (r != null) builder.append(r.getAsMention()).append(": `").append(String.join("`, `", entry.getValue())).append("`").append("\n");
            }
            event.reply(builder.toString()).setEphemeral(true).queue();
            return;
        }

        // Specific status role
        final Set<String> statuses = coGuild.statusRoles.get(role.getIdLong());
        if (statuses == null || statuses.isEmpty()) {
            event.reply(role.getAsMention() + " is not linked to any statuses").setEphemeral(true).queue();
            return;
        }
        event.reply(role.getAsMention() + " is linked to these statuses: `" + String.join("`, `", statuses) + "`").setEphemeral(true).queue();
    }

    @JDASlashCommand(
            scope = CommandScope.GUILD,
            name = "statusrole",
            subcommand = "add",
            description = "Set the role that will be given to users who have specific status")
    public void addCommand(@NotNull GuildSlashEvent event,
                           @AppOption(description = "The role to link to the status") @NotNull Role role,
                           @AppOption(description = "The status(es) to link to the role") @NotNull String status,
                           @AppOption(description = "The characters to split multiple statuses") @Nullable String delimiter) {
        status = status.toLowerCase().trim();

        // Get statuses
        Set<String> statuses = Set.of(status);
        if (delimiter != null) statuses = Arrays.stream(status.split(delimiter))
                .map(String::trim)
                .collect(Collectors.toSet());

        // Add to statusRoles and reply
        cobalt.data.getGuild(event.getGuild()).statusRoles.computeIfAbsent(role.getIdLong(), s -> new HashSet<>()).addAll(statuses);
        event.reply(role.getAsMention() + " will now be given to users who have any of these phrases in their status: `" + String.join("`, `", statuses) + "`").setEphemeral(true).queue();
    }

    @JDASlashCommand(
            scope = CommandScope.GUILD,
            name = "statusrole",
            subcommand = "remove",
            description = "Remove the specified status role")
    public void removeCommand(@NotNull GuildSlashEvent event,
                              @AppOption(description = "The role to unlink from the status") @NotNull Role role,
                              @AppOption(description = "The status(es) to unlink from the role", autocomplete = AC_REMOVE_STATUS) @NotNull String status) {
        status = status.toLowerCase().trim();

        // Get statuses
        final Set<String> statuses = cobalt.data.getGuild(event.getGuild()).statusRoles.get(role.getIdLong());
        if (statuses == null || !statuses.contains(status)) {
            event.reply(role.getAsMention() + " is not linked to the status `" + status + "`").setEphemeral(true).queue();
            return;
        }

        // Remove from statusRoles
        statuses.remove(status);
        event.reply(role.getAsMention() + " will no longer be given to users who have `" + status + "` in their status").setEphemeral(true).queue();
    }

    @AutocompletionHandler(name = AC_REMOVE_STATUS) @NotNull
    public List<String> acRemoveStatus(@NotNull CommandAutoCompleteInteractionEvent event) {
        final OptionMapping roleOption = event.getOption("role");
        final Guild guild = event.getGuild();
        if (roleOption == null || guild == null) return Collections.emptyList();
        final Set<String> statuses = cobalt.data.getGuild(guild).statusRoles.get(roleOption.getAsLong());
        if (statuses == null) return Collections.emptyList();
        return new ArrayList<>(statuses);
    }
}

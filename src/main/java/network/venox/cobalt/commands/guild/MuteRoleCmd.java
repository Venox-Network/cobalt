package network.venox.cobalt.commands.guild;

import com.freya02.botcommands.api.annotations.CommandMarker;
import com.freya02.botcommands.api.annotations.Dependency;
import com.freya02.botcommands.api.annotations.UserPermissions;
import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.CommandScope;
import com.freya02.botcommands.api.application.annotations.AppOption;
import com.freya02.botcommands.api.application.slash.GuildSlashEvent;
import com.freya02.botcommands.api.application.slash.annotations.JDASlashCommand;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;

import network.venox.cobalt.Cobalt;
import network.venox.cobalt.data.CoGuild;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


@CommandMarker @UserPermissions({Permission.MANAGE_ROLES, Permission.MODERATE_MEMBERS})
public class MuteRoleCmd extends ApplicationCommand {
    @Dependency private Cobalt cobalt;

    @JDASlashCommand(
            scope = CommandScope.GUILD,
            name = "muterole",
            subcommand = "set",
            description = "Set the mute role for the guild")
    public void setCommand(@NotNull GuildSlashEvent event,
                           @AppOption(description = "The role to set as the mute role") @Nullable Role role) {
        final CoGuild guild = cobalt.data.getGuild(event.getGuild());

        // Remove muteRole
        if (role == null) {
            if (guild.muteRole == null) {
                event.reply("There currently isn't a mute role").setEphemeral(true).queue();
                return;
            }

            event.reply("<@&" + guild.muteRole + "> has been unset as the mute role").setEphemeral(true).queue();
            guild.muteRole = null;
            return;
        }

        // Set muteRole
        guild.muteRole = role.getIdLong();
        event.reply("Mute role set to " + role.getAsMention()).setEphemeral(true).queue();
    }

    @JDASlashCommand(
            scope = CommandScope.GUILD,
            name = "muterole",
            subcommand = "get",
            description = "Get the mute role for the guild")
    public void getCommand(@NotNull GuildSlashEvent event) {
        final CoGuild guild = cobalt.data.getGuild(event.getGuild());

        // No muteRole
        if (guild.muteRole == null) {
            event.reply("There currently isn't a mute role").setEphemeral(true).queue();
            return;
        }

        // Get muteRole
        event.reply("<@&" + guild.muteRole + "> is the current mute role").setEphemeral(true).queue();
    }
}

package network.venox.cobalt.commands.guild.muterole;

import com.freya02.botcommands.api.annotations.CommandMarker;
import com.freya02.botcommands.api.annotations.Dependency;
import com.freya02.botcommands.api.annotations.UserPermissions;
import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.slash.GuildSlashEvent;
import com.freya02.botcommands.api.application.slash.annotations.JDASlashCommand;

import net.dv8tion.jda.api.Permission;

import network.venox.cobalt.Cobalt;
import network.venox.cobalt.mongo.Server;

import org.jetbrains.annotations.NotNull;

import xyz.srnyx.lazylibrary.LazyEmoji;


@CommandMarker @UserPermissions({Permission.MANAGE_ROLES, Permission.MODERATE_MEMBERS})
public class MuteRoleGet extends ApplicationCommand {
    @Dependency private Cobalt bot;

    @JDASlashCommand(
            name = "muterole",
            subcommand = "get",
            description = "Get the mute role for the guild")
    public void getCommand(@NotNull GuildSlashEvent event) {
        final Long muteRole = bot.mongo.getMagicCollection(Server.class)
                .findOne("_id", event.getGuild().getIdLong())
                .map(server -> server.muteRole)
                .orElse(null);

        // No mute role
        if (muteRole == null) {
            event.reply(LazyEmoji.NO + " There isn't a mute role configured!").setEphemeral(true).queue();
            return;
        }

        // Get mute role
        event.reply(LazyEmoji.YES + " <@&" + muteRole + "> is the current mute role").setEphemeral(true).queue();
    }
}

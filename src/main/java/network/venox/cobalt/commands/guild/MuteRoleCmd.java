package network.venox.cobalt.commands.guild;

import com.freya02.botcommands.api.annotations.CommandMarker;
import com.freya02.botcommands.api.annotations.Dependency;
import com.freya02.botcommands.api.annotations.UserPermissions;
import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.annotations.AppOption;
import com.freya02.botcommands.api.application.slash.GuildSlashEvent;
import com.freya02.botcommands.api.application.slash.annotations.JDASlashCommand;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;

import network.venox.cobalt.Cobalt;
import network.venox.cobalt.mongo.Server;

import org.bson.conversions.Bson;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.lazylibrary.LazyEmoji;

import xyz.srnyx.magicmongo.MagicCollection;


@CommandMarker @UserPermissions({Permission.MANAGE_ROLES, Permission.MODERATE_MEMBERS})
public class MuteRoleCmd extends ApplicationCommand {
    @Dependency private Cobalt bot;

    @JDASlashCommand(
            name = "muterole",
            subcommand = "get",
            description = "Get the mute role for the guild")
    public void getCommand(@NotNull GuildSlashEvent event) {
        final Long muteRole = bot.dataManager.mongo.getMagicCollection(Server.class)
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

    @JDASlashCommand(
            name = "muterole",
            subcommand = "set",
            description = "Set the mute role for the guild")
    public void setCommand(@NotNull GuildSlashEvent event,
                           @AppOption(description = "The role to set as the mute role") @Nullable Role role) {
        final MagicCollection<Server> collection = bot.dataManager.mongo.getMagicCollection(Server.class);
        final Bson filter = Filters.eq("_id", event.getGuild().getIdLong());

        // Remove mute role
        if (role == null) {
            final Server server = collection.findOneAndUpdate(filter, Updates.unset(Server.PROP_MUTE_ROLE));
            final Long muteRoleId = server == null ? null : server.muteRole;

            // No mute role
            if (muteRoleId == null) {
                event.reply(LazyEmoji.NO + " There isn't a mute role configured!").setEphemeral(true).queue();
                return;
            }

            // Reply
            event.reply(LazyEmoji.YES + " <@&" + muteRoleId + "> has been unset as the mute role").setEphemeral(true).queue();
            return;
        }

        // Set mute role
        collection.updateOne(filter, Updates.set(Server.PROP_MUTE_ROLE, role.getIdLong()));
        event.reply(LazyEmoji.YES + " Mute role set to " + role.getAsMention()).setEphemeral(true).queue();
    }
}

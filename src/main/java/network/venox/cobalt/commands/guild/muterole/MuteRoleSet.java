package network.venox.cobalt.commands.guild.muterole;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;

import io.github.freya022.botcommands.api.commands.annotations.Command;
import io.github.freya022.botcommands.api.commands.annotations.UserPermissions;
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand;
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashOption;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;

import network.venox.cobalt.MongoProvider;
import network.venox.cobalt.mongo.Server;

import org.bson.conversions.Bson;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.lazylibrary.LazyEmoji;

import xyz.srnyx.magicmongo.MagicCollection;


@Command
public class MuteRoleSet extends ApplicationCommand {
    @NotNull private final MongoProvider mongo;

    public MuteRoleSet(@NotNull MongoProvider mongo) {
        this.mongo = mongo;
    }

    @UserPermissions({Permission.MANAGE_ROLES, Permission.MODERATE_MEMBERS})
    @JDASlashCommand(
            name = "muterole",
            subcommand = "set",
            description = "Set the mute role for the guild")
    public void setCommand(@NotNull GuildSlashEvent event,
                           @SlashOption(description = "The role to set as the mute role") @Nullable Role role) {
        final MagicCollection<Server> collection = mongo.database.getMagicCollection(Server.class);
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

        // Check user hierarchy
        if (!event.getMember().canInteract(role)) {
            event.reply(LazyEmoji.NO + " You cannot set the mute role to " + role.getAsMention() + " due to hierarchy!").setEphemeral(true).queue();
            return;
        }

        // Check self hierarchy
        if (!event.getGuild().getSelfMember().canInteract(role)) {
            event.reply(LazyEmoji.NO + " I cannot set the mute role to " + role.getAsMention() + " due to hierarchy!").setEphemeral(true).queue();
            return;
        }

        // Set mute role
        collection.updateOne(filter, Updates.set(Server.PROP_MUTE_ROLE, role.getIdLong()));
        event.reply(LazyEmoji.YES + " Mute role set to " + role.getAsMention()).setEphemeral(true).queue();
    }
}

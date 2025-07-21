package network.venox.cobalt.commands.guild.locking.preset;

import com.freya02.botcommands.api.annotations.CommandMarker;
import com.freya02.botcommands.api.annotations.Dependency;
import com.freya02.botcommands.api.annotations.UserPermissions;
import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.CommandScope;
import com.freya02.botcommands.api.application.annotations.AppOption;
import com.freya02.botcommands.api.application.slash.GuildSlashEvent;
import com.freya02.botcommands.api.application.slash.annotations.JDASlashCommand;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;

import network.venox.cobalt.Cobalt;
import network.venox.cobalt.commands.guild.locking.LockingCommon;
import network.venox.cobalt.mongo.Server;

import org.bson.conversions.Bson;

import org.jetbrains.annotations.NotNull;

import xyz.srnyx.lazylibrary.LazyEmoji;

import xyz.srnyx.magicmongo.MagicCollection;


@CommandMarker @UserPermissions({Permission.MANAGE_CHANNEL, Permission.MANAGE_PERMISSIONS, Permission.MANAGE_SERVER, Permission.MANAGE_ROLES})
public class LockingPresetAddRole extends ApplicationCommand {
    @Dependency private Cobalt bot;

    @JDASlashCommand(
            scope = CommandScope.GUILD,
            name = "locking",
            group = "preset",
            subcommand = "addrole",
            description = "ADMIN | Add a role to a locking preset (or create a new preset)")
    public void lockingPresetAddRole(@NotNull GuildSlashEvent event,
                                     @AppOption(description = "The preset to add the role to (or a new one)", autocomplete = LockingCommon.AC_PRESET) @NotNull String preset,
                                     @AppOption(description = "The role to add") @NotNull Role role) {
        final MagicCollection<Server> collection = bot.mongo.getMagicCollection(Server.class);
        final Bson filter = Filters.eq("_id", event.getGuild().getIdLong());

        // Check if role already in preset
        if (collection.countDocuments(Filters.and(
                filter,
                Filters.in(Server.PROP_LOCK_PRESETS + "." + preset, role.getIdLong()))) != 0) {
            event.reply(LazyEmoji.NO + " " + role.getAsMention() + " is already in the locking preset `" + preset + "`!").setEphemeral(true).queue();
            return;
        }

        collection.upsertOne(
                filter,
                Updates.addToSet(Server.PROP_LOCK_PRESETS + "." + preset, role.getIdLong()));
        event.reply(LazyEmoji.YES + " Added " + role.getAsMention() + " to the locking preset `" + preset + "`").setEphemeral(true).queue();
    }
}

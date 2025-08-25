package network.venox.cobalt.commands.guild.locking.preset;

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
import network.venox.cobalt.commands.guild.locking.LockingCommon;
import network.venox.cobalt.mongo.Server;

import org.bson.conversions.Bson;

import org.jetbrains.annotations.NotNull;

import xyz.srnyx.lazylibrary.LazyEmoji;

import xyz.srnyx.magicmongo.MagicCollection;


@Command
public class LockingPresetAddRole extends ApplicationCommand {
    @NotNull private final MongoProvider mongo;

    public LockingPresetAddRole(@NotNull MongoProvider mongo) {
        this.mongo = mongo;
    }

    @UserPermissions({Permission.MANAGE_CHANNEL, Permission.MANAGE_PERMISSIONS, Permission.MANAGE_SERVER, Permission.MANAGE_ROLES})
    @JDASlashCommand(
            name = "locking",
            group = "preset",
            subcommand = "addrole",
            description = "ADMIN | Add a role to a locking preset (or create a new preset)")
    public void lockingPresetAddRole(@NotNull GuildSlashEvent event,
                                     @SlashOption(description = "The preset to add the role to (or a new one)", autocomplete = LockingCommon.AC_PRESET) @NotNull String preset,
                                     @SlashOption(description = "The role to add") @NotNull Role role) {
        final MagicCollection<Server> collection = mongo.database.getMagicCollection(Server.class);
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

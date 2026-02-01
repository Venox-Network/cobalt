package network.venox.cobalt.commands.guild.locking.preset;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;

import io.github.freya022.botcommands.api.commands.annotations.UserPermissions;
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashOption;
import io.github.freya022.botcommands.api.commands.application.slash.autocomplete.annotations.AutocompleteHandler;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;

import network.venox.cobalt.MongoProvider;
import network.venox.cobalt.commands.guild.locking.LockingCommon;
import network.venox.cobalt.mongo.Server;

import org.bson.conversions.Bson;

import org.jetbrains.annotations.NotNull;

import xyz.srnyx.javautilities.manipulation.Mapper;
import xyz.srnyx.lazylibrary.LazyEmbed;
import xyz.srnyx.lazylibrary.LazyEmoji;
import xyz.srnyx.lazylibrary.utility.LazyUtilities;

import xyz.srnyx.magicmongo.MagicCollection;

import java.util.*;


@io.github.freya022.botcommands.api.commands.annotations.Command
public class LockingPresetRemoveRole {
    @NotNull private static final String AC_ROLE = "LockingPresetRemoveRole.ac.role";

    @NotNull private final MongoProvider mongo;

    public LockingPresetRemoveRole(@NotNull MongoProvider mongo) {
        this.mongo = mongo;
    }

    @UserPermissions({Permission.MANAGE_CHANNEL, Permission.MANAGE_PERMISSIONS, Permission.MANAGE_SERVER, Permission.MANAGE_ROLES})
    @JDASlashCommand(
            name = "locking",
            group = "preset",
            subcommand = "removerole",
            description = "ADMIN | Remove a role from a locking preset")
    public void lockingPresetRemoveRole(@NotNull GuildSlashEvent event,
                                        @SlashOption(description = "The preset to remove the role from", autocomplete = LockingCommon.AC_PRESET) @NotNull String preset,
                                        @SlashOption(description = "The ID of the role to remove", autocomplete = AC_ROLE) @NotNull String role) {
        // Get role ID as long
        final Long roleId = Mapper.toLong(role).orElse(null);
        if (roleId == null) {
            event.replyEmbeds(LazyEmbed.invalidArgument("role", role).build()).setEphemeral(true).queue();
            return;
        }

        final MagicCollection<Server> collection = mongo.database.getMagicCollection(Server.class);
        final Bson filter = Filters.eq("_id", event.getGuild().getIdLong());
        final String lockPresetPath = Server.PROP_LOCK_PRESETS + "." + preset;

        // Get lockPresets
        final Optional<Server> server = mongo.database.getMagicCollection(Server.class).findOne(Filters.and(
                filter,
                Filters.exists(lockPresetPath),
                Filters.ne(lockPresetPath, Set.of())));
        if (server.isEmpty()) {
            event.reply(LazyEmoji.NO + " The locking preset `" + preset + "` doesn't exist!").setEphemeral(true).queue();
            return;
        }

        // Remove role
        final Set<Long> roles = Objects.requireNonNull(server.get().lockPresets).get(preset);
        if (!roles.remove(roleId)) {
            event.reply(LazyEmoji.NO + " <@&" + role + "> is not in the locking preset `" + preset + "`!").setEphemeral(true).queue();
            return;
        }

        // Remove preset if empty
        if (roles.isEmpty()) {
            collection.updateOne(
                    filter,
                    Updates.unset(lockPresetPath));
            event.reply(LazyEmoji.YES + " Removed the locking preset `" + preset + "` as it is now empty").setEphemeral(true).queue();
            return;
        }

        // Update preset
        collection.updateOne(
                filter,
                Updates.pull(lockPresetPath, roleId));
        event.reply(LazyEmoji.YES + " Removed <@&" + role + "> from the locking preset `" + preset + "`").setEphemeral(true).queue();
    }

    @AutocompleteHandler(AC_ROLE) @NotNull
    public List<Command.Choice> onAutoCompleteServer(@NotNull CommandAutoCompleteInteractionEvent event,
                                                     @SlashOption String preset) {
        final Guild guild = Objects.requireNonNull(event.getGuild());
        final String lockPresetPath = Server.PROP_LOCK_PRESETS + "." + preset;
        return LazyUtilities.sortChoicesFuzzy(event, mongo.database.getMagicCollection(Server.class)
                .findOne(Filters.and(
                        Filters.eq("_id", guild.getIdLong()),
                        Filters.exists(lockPresetPath),
                        Filters.ne(lockPresetPath, Set.of())))
                .map(server -> Objects.requireNonNull(server.lockPresets).get(preset).stream()
                        .map(guild::getRoleById)
                        .filter(Objects::nonNull)
                        .map(role -> new Command.Choice("@" + role.getName(), role.getIdLong()))
                        .toList())
                .orElse(Collections.emptyList()));
    }
}

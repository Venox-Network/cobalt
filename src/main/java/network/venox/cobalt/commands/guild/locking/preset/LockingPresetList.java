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

import net.dv8tion.jda.api.Permission;

import network.venox.cobalt.Cobalt;
import network.venox.cobalt.commands.guild.locking.LockingCommon;
import network.venox.cobalt.mongo.Server;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.lazylibrary.LazyEmoji;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;


@CommandMarker @UserPermissions({Permission.MANAGE_CHANNEL, Permission.MANAGE_PERMISSIONS, Permission.MANAGE_SERVER, Permission.MANAGE_ROLES})
public class LockingPresetList extends ApplicationCommand {
    @Dependency private Cobalt bot;

    @JDASlashCommand(
            scope = CommandScope.GUILD,
            name = "locking",
            group = "preset",
            subcommand = "list",
            description = "ADMIN | List all locking presets or the roles of a preset")
    public void lockingPresetList(@NotNull GuildSlashEvent event,
                                  @AppOption(description = "The preset to get the roles of", autocomplete = LockingCommon.AC_PRESET) @Nullable String preset) {
        // Get server
        final Optional<Server> server = bot.mongo.getMagicCollection(Server.class).findOne(Filters.and(
                Filters.eq("_id", event.getGuild().getIdLong()),
                Filters.exists(Server.PROP_LOCK_PRESETS),
                Filters.ne(Server.PROP_LOCK_PRESETS, Map.of())));
        if (server.isEmpty()) {
            event.reply(LazyEmoji.NO + " No locking presets found!").setEphemeral(true).queue();
            return;
        }
        final Map<String, Set<Long>> presets = Objects.requireNonNull(server.get().lockPresets);

        // Specific preset
        if (preset != null) {
            // Get roles
            final Set<Long> roles = presets.get(preset);
            if (roles == null || roles.isEmpty()) {
                event.reply(LazyEmoji.NO + " The preset `" + preset + "` doesn't exist!").setEphemeral(true).queue();
                return;
            }

            // Reply
            final StringBuilder reply = new StringBuilder(LazyEmoji.YES + " **Roles for preset `" + preset + "`:**\n");
            for (final long roleId : roles) reply.append("<@&").append(roleId).append(">\n");
            event.reply(reply.toString()).setEphemeral(true).queue();
            return;
        }

        // All presets
        final StringBuilder reply = new StringBuilder(LazyEmoji.YES + " **All locking presets**\n");
        for (final Map.Entry<String, Set<Long>> entry : presets.entrySet()) {
            reply.append("- `").append(entry.getKey()).append("`: ");
            for (final long roleId : entry.getValue()) reply.append("<@&").append(roleId).append("> ");
            reply.append("\n");
        }
        event.reply(reply.toString()).setEphemeral(true).queue();
    }
}

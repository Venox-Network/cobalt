package network.venox.cobalt.commands.guild.locking;

import com.freya02.botcommands.api.annotations.CommandMarker;
import com.freya02.botcommands.api.annotations.Dependency;
import com.freya02.botcommands.api.annotations.UserPermissions;
import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.annotations.AppOption;
import com.freya02.botcommands.api.application.slash.GuildSlashEvent;
import com.freya02.botcommands.api.application.slash.annotations.JDASlashCommand;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.PermissionOverride;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import network.venox.cobalt.Cobalt;
import network.venox.cobalt.mongo.Lock;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.lazylibrary.LazyEmoji;
import xyz.srnyx.lazylibrary.utility.LazyUtilities;

import xyz.srnyx.magicmongo.MagicCollection;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;


@CommandMarker @UserPermissions({Permission.MANAGE_CHANNEL, Permission.MANAGE_PERMISSIONS})
public class LockingUnlock extends ApplicationCommand {
    @Dependency private Cobalt bot;

    @JDASlashCommand(
            name = "locking",
            subcommand = "unlock",
            description = "ADMIN | Unlock the channel back to normal")
    public void unlock(@NotNull GuildSlashEvent event,
                       @AppOption(description = "The channel to unlock") @Nullable TextChannel channel) {
        channel = LockingCommon.initialize(event, channel);
        if (channel == null) return;
        final MagicCollection<Lock> collection = bot.mongo.getMagicCollection(Lock.class);

        // Check if channel is locked
        final Lock lock = collection.findOne("_id", channel.getIdLong()).orElse(null);
        if (lock == null) {
            event.reply(LazyEmoji.NO + " This channel is not locked!").setEphemeral(true).queue();
            return;
        }

        // Delete from database
        collection.deleteOne("_id", lock.channel);

        final Guild guild = channel.getGuild();
        for (final Map.Entry<String, Lock.PreviousPermissions> entry : lock.previousPermissions.entrySet()) {
            // Get override
            final long roleId = Long.parseLong(entry.getKey());
            final Role role = guild.getRoleById(roleId);
            if (role == null) continue;
            final PermissionOverride override = channel.getPermissionOverride(role);
            if (override == null) continue;

            // Overwrite current locked permissions based on previous
            final Set<Permission> newAllowed = new HashSet<>(override.getAllowed());
            final Set<Permission> newDenied = new HashSet<>(override.getDenied());
            final Lock.PreviousPermissions previous = entry.getValue();
            final Set<Permission> previousAllowed = previous.allowed;
            final Set<Permission> previousDenied = previous.denied;
            for (final Permission permission : LockingCommon.PERMISSIONS) {
                if (previousAllowed.contains(permission)) {
                    newAllowed.add(permission);
                    newDenied.remove(permission);
                } else if (previousDenied.contains(permission)) {
                    newAllowed.remove(permission);
                    newDenied.add(permission);
                } else {
                    newAllowed.remove(permission);
                    newDenied.remove(permission);
                }
            }

            override.getManager().setPermissions(newAllowed, newDenied).queue();
        }

        // Delete sticky message
        final ScheduledFuture<?> scheduler = Lock.LOCK_FUTURES.remove(channel.getIdLong());
        if (scheduler != null) scheduler.cancel(false);
        lock.deleteStickyMessage(channel).ifPresent(action -> action.queue(null, LazyUtilities.IGNORE_UNKNOWN_MESSAGE));

        // Reply
        event.reply(LazyEmoji.YES + " Unlocked " + channel.getAsMention()).queue();
    }
}

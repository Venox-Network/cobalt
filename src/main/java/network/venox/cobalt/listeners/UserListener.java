package network.venox.cobalt.listeners;

import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.user.update.UserUpdateActivitiesEvent;

import network.venox.cobalt.CoListener;
import network.venox.cobalt.Cobalt;
import network.venox.cobalt.mongo.Server;

import org.jetbrains.annotations.NotNull;

import xyz.srnyx.lazylibrary.utility.LazyUtilities;

import java.util.List;
import java.util.Map;


public class UserListener extends CoListener {
    public UserListener(@NotNull Cobalt bot) {
        super(bot);
    }

    @Override
    public void onUserUpdateActivities(@NotNull UserUpdateActivitiesEvent event) {
        final Member member = event.getMember();
        if (member.getUser().isBot()) return;
        final Guild guild = member.getGuild();

        // Get statusRoles
        final Map<String, Long> statusRoles = bot.dataManager.mongo.getMagicCollection(Server.class).findOne("_id", guild.getIdLong())
                .map(server -> server.statusRoles)
                .orElse(Map.of());
        if (statusRoles.isEmpty()) return;

        // Check each status role
        for (final Map.Entry<String, Long> entry : statusRoles.entrySet()) {
            // Add role if user has status
            final List<Activity> activities = event.getNewValue();
            if (activities != null && activities.stream().anyMatch(activity -> activity.getType() == Activity.ActivityType.CUSTOM_STATUS && activity.getName().toLowerCase().contains(entry.getKey()))) {
                final Role role = guild.getRoleById(entry.getValue());
                if (role != null && !member.getRoles().contains(role)) guild.addRoleToMember(member, role).queue(null, LazyUtilities.IGNORE_UNKNOWN_MEMBER);
                return;
            }

            // Remove role if user doesn't have status
            final Role role = guild.getRoleById(entry.getValue());
            if (role != null && member.getRoles().contains(role)) guild.removeRoleFromMember(member, role).queue(null, LazyUtilities.IGNORE_UNKNOWN_MEMBER);
        }
    }
}

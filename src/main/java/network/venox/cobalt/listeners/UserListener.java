package network.venox.cobalt.listeners;

import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.user.UserActivityEndEvent;
import net.dv8tion.jda.api.events.user.UserActivityStartEvent;

import network.venox.cobalt.CoListener;
import network.venox.cobalt.Cobalt;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;


public class UserListener extends CoListener {
    public UserListener(@NotNull Cobalt cobalt) {
        super(cobalt);
    }

    @Override
    public void onUserActivityStart(@NotNull UserActivityStartEvent event) {
        final Activity activity = event.getNewActivity();
        if (!activity.getType().equals(Activity.ActivityType.CUSTOM_STATUS)) return;
        final Guild guild = event.getGuild();
        final Set<Map.Entry<Role, Set<String>>> entries = cobalt.data.getGuild(guild).statusRoles.entrySet().stream()
                .map(entry -> {
                    final Role role = guild.getRoleById(entry.getKey());
                    return role == null ? null : Map.entry(role, entry.getValue());
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // Check status roles
        final Member member = event.getMember();
        final List<Role> memberRoles = member.getRoles();
        final String status = activity.getName().toLowerCase().trim();
        for (final Map.Entry<Role, Set<String>> entry : entries) {
            final Role role = entry.getKey();
            final boolean hasRole = memberRoles.contains(role);
            if (entry.getValue().stream().anyMatch(status::contains)) {
                if (!hasRole) guild.addRoleToMember(member, role).queue();
                continue;
            }
            if (hasRole) guild.removeRoleFromMember(member, role).queue();
        }
    }

    @Override
    public void onUserActivityEnd(@NotNull UserActivityEndEvent event) {
        if (!event.getOldActivity().getType().equals(Activity.ActivityType.CUSTOM_STATUS)) return;
        final Guild guild = event.getGuild();
        final Member member = event.getMember();
        cobalt.data.getGuild(event.getGuild()).statusRoles.keySet().stream()
                .map(guild::getRoleById)
                .filter(Objects::nonNull)
                .forEach(role -> guild.removeRoleFromMember(member, role).queue());
    }
}

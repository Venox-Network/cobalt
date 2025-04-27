package network.venox.cobalt.listeners;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

import network.venox.cobalt.CoListener;
import network.venox.cobalt.Cobalt;

import org.jetbrains.annotations.NotNull;

import xyz.srnyx.lazylibrary.events.GuildVoiceJoinEvent;
import xyz.srnyx.lazylibrary.events.GuildVoiceLeaveEvent;

import java.util.Set;


public class GuildVoiceListener extends CoListener {
    public GuildVoiceListener(@NotNull Cobalt cobalt) {
        super(cobalt);
    }

    @Override
    public void onGuildVoiceJoin(@NotNull GuildVoiceJoinEvent event) {
        final Guild guild = event.getGuild();
        final CoGuild coGuild = bot.oldData.getGuild(guild);
        final Member member = event.getMember();

        // Mute role
        final Role muteRole = coGuild.getMuteRole();
        if (muteRole == null) return;
        // Mute
        if (member.getRoles().contains(muteRole)) {
            final GuildVoiceState voiceState = member.getVoiceState();
            if (voiceState == null || voiceState.isGuildMuted()) return;
            guild.mute(member, true).queue();
            coGuild.mutedUsers.add(member.getIdLong());
            return;
        }
        // Unmute
        if (coGuild.mutedUsers.contains(member.getIdLong())) {
            guild.mute(member, false).queue();
            coGuild.mutedUsers.remove(member.getIdLong());
        }
    }

    @Override
    public void onGuildVoiceLeave(@NotNull GuildVoiceLeaveEvent event) {
        // Voice roles
        final Guild guild = event.getGuild();
        final Set<Role> roles = bot.oldData.getGuild(guild).getVoiceRoles(event.getChannelLeft().getIdLong());
        if (roles == null) return;
        final Member member = event.getMember();
        roles.forEach(role -> guild.removeRoleFromMember(member, role).queue());
    }
}

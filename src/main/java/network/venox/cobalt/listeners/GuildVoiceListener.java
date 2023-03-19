package network.venox.cobalt.listeners;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;

import network.venox.cobalt.CoListener;
import network.venox.cobalt.Cobalt;
import network.venox.cobalt.data.CoGuild;
import network.venox.cobalt.events.GuildVoiceJoinEvent;
import network.venox.cobalt.events.GuildVoiceLeaveEvent;

import org.jetbrains.annotations.NotNull;

import java.util.Set;


public class GuildVoiceListener extends CoListener {
    public GuildVoiceListener(@NotNull Cobalt cobalt) {
        super(cobalt);
    }

    @Override
    public void onGuildVoiceUpdate(@NotNull GuildVoiceUpdateEvent event) {
        // Joined
        if (event.getChannelJoined() != null) {
            onGuildVoiceJoin(new GuildVoiceJoinEvent(event));
            return;
        }
        // Left
        if (event.getChannelLeft() != null) onGuildVoiceLeave(new GuildVoiceLeaveEvent(event));
    }

    private void onGuildVoiceJoin(@NotNull GuildVoiceJoinEvent event) {
        final Guild guild = event.getGuild();
        final CoGuild coGuild = cobalt.data.getGuild(guild);
        final Member member = event.getMember();

        // Voice roles
        final Set<Role> roles = coGuild.getVoiceRoles(event.getChannelJoined().getIdLong());
        if (roles != null) roles.forEach(role -> guild.addRoleToMember(member, role).queue());

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

    private void onGuildVoiceLeave(@NotNull GuildVoiceLeaveEvent event) {
        // Voice roles
        final Guild guild = event.getGuild();
        final Set<Role> roles = cobalt.data.getGuild(guild).getVoiceRoles(event.getChannelLeft().getIdLong());
        if (roles == null) return;
        final Member member = event.getMember();
        roles.forEach(role -> guild.removeRoleFromMember(member, role).queue());
    }
}

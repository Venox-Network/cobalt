package network.venox.cobalt.listeners;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;

import network.venox.cobalt.CoListener;
import network.venox.cobalt.Cobalt;
import network.venox.cobalt.data.CoGuild;

import org.jetbrains.annotations.NotNull;

import java.util.Set;


public class GuildVoiceListener extends CoListener {
    public GuildVoiceListener(@NotNull Cobalt cobalt) {
        super(cobalt);
    }

    @Override
    public void onGuildVoiceUpdate(@NotNull GuildVoiceUpdateEvent event) {
        final Guild guild = event.getGuild();
        final CoGuild coGuild = cobalt.data.getGuild(guild);
        final Member member = event.getMember();

        // Joined
        final AudioChannel joined = event.getChannelJoined();
        if (joined != null) {
            final Set<Role> roles = coGuild.getVoiceRoles(joined.getIdLong());
            if (roles != null) roles.forEach(role -> guild.addRoleToMember(member, role).queue());
            return;
        }

        // Left
        final AudioChannel left = event.getChannelLeft();
        if (left != null) {
            final Set<Role> roles = coGuild.getVoiceRoles(left.getIdLong());
            if (roles != null) roles.forEach(role -> guild.removeRoleFromMember(member, role).queue());
        }
    }
}

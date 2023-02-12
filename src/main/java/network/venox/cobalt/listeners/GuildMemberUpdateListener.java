package network.venox.cobalt.listeners;

import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateNicknameEvent;

import network.venox.cobalt.CoListener;
import network.venox.cobalt.Cobalt;
import network.venox.cobalt.data.CoGuild;

import org.jetbrains.annotations.NotNull;


public class GuildMemberUpdateListener extends CoListener {
    public GuildMemberUpdateListener(@NotNull Cobalt cobalt) {
        super(cobalt);
    }

    @Override
    public void onGuildMemberUpdateNickname(@NotNull GuildMemberUpdateNicknameEvent event) {
        final String nickname = event.getNewNickname();
        if (nickname == null) return;
        final CoGuild guild = cobalt.data.getGuild(event.getGuild());
        if (guild.nicknameBlacklist.contains(nickname.toLowerCase())) event.getMember().modifyNickname(guild.getModeratedNickname()).queue();
    }
}

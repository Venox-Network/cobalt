package network.venox.cobalt.listeners;

import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateNicknameEvent;

import network.venox.cobalt.CoListener;
import network.venox.cobalt.Cobalt;

import org.jetbrains.annotations.NotNull;


public class GuildMemberUpdateListener extends CoListener {
    public GuildMemberUpdateListener(@NotNull Cobalt cobalt) {
        super(cobalt);
    }

    @Override
    public void onGuildMemberUpdateNickname(@NotNull GuildMemberUpdateNicknameEvent event) {
        cobalt.data.getGuild(event.getGuild()).checkMemberNickname(event.getMember(), null);
    }
}

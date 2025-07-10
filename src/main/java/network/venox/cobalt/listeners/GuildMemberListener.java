package network.venox.cobalt.listeners;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;

import network.venox.cobalt.CoListener;
import network.venox.cobalt.Cobalt;

import org.jetbrains.annotations.NotNull;


public class GuildMemberListener extends CoListener {
    public GuildMemberListener(@NotNull Cobalt cobalt) {
        super(cobalt);
    }

    @Override
    public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
        final Guild guild = event.getGuild();
        final Member member = event.getMember();
        final User user = event.getUser();
        final CoGuild coGuild = bot.oldData.getGuild(guild);

        // Welcome message
        coGuild.sendWelcomeMessage(user);

        // Increase stats
        coGuild.memberCount++;
        if (!user.isBot()) coGuild.humanCount++;
    }
}

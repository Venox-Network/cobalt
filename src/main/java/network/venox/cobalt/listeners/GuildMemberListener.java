package network.venox.cobalt.listeners;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;

import network.venox.cobalt.CoListener;
import network.venox.cobalt.Cobalt;
import network.venox.cobalt.data.objects.CoSuperBan;

import org.jetbrains.annotations.NotNull;

import xyz.srnyx.lazylibrary.LazyEmbed;

import java.util.concurrent.TimeUnit;


public class GuildMemberListener extends CoListener {
    public GuildMemberListener(@NotNull Cobalt cobalt) {
        super(cobalt);
    }

    @Override
    public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
        final Guild guild = event.getGuild();
        final Member member = event.getMember();
        final User user = event.getUser();

        // Check if user is super-banned
        final CoSuperBan ban = bot.oldData.global.superBans.stream()
                .filter(b -> b.user == member.getIdLong())
                .findFirst()
                .orElse(null);
        if (ban != null) {
            // Check if ban is expired
            final Long time = ban.time;
            if (time != null && time - System.currentTimeMillis() <= 0) {
                ban.unban();
                return;
            }

            // Send embed and ban user
            ban.getModerator()
                    .flatMap(moderator -> user.openPrivateChannel()
                            .flatMap(channel -> channel.sendMessageEmbeds(new LazyEmbed()
                                    .setTitle("You're banned from all Venox servers")
                                    .setDescription("You can't join `" + guild.getName() + "` because you're banned from *all* **Venox servers**")
                                    .addField("Reason", ban.reason, true)
                                    .addField("Time left", ban.getTimeLeft(), true)
                                    .build(bot))))
                    .flatMap(message -> guild.ban(member, 1, TimeUnit.DAYS).reason(ban.reason))
                    .queue();
            return;
        }
        final CoGuild coGuild = bot.oldData.getGuild(guild);

        // Welcome message
        coGuild.sendWelcomeMessage(user);

        // Increase stats
        coGuild.memberCount++;
        if (!user.isBot()) coGuild.humanCount++;
    }
}

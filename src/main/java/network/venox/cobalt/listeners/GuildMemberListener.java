package network.venox.cobalt.listeners;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;

import network.venox.cobalt.CoGuildStats;
import network.venox.cobalt.CoListener;
import network.venox.cobalt.Cobalt;
import network.venox.cobalt.data.CoGuild;
import network.venox.cobalt.data.objects.CoSuperBan;

import org.jetbrains.annotations.NotNull;

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
        final CoSuperBan ban = cobalt.data.global.superBans.stream()
                .filter(b -> b.user() == member.getIdLong())
                .findFirst()
                .orElse(null);
        if (ban != null) {
            // Check if ban is expired
            final Long time = ban.time();
            if (time != null && time - System.currentTimeMillis() <= 0) {
                ban.unban();
                return;
            }

            // Send embed and ban user
            ban.getModerator()
                    .flatMap(moderate -> user.openPrivateChannel()
                            .flatMap(channel -> channel.sendMessageEmbeds(cobalt.messages.getEmbed("super", "ban")
                                    .replace("%guild%", guild.getName())
                                    .replace("%reason%", ban.reason())
                                    .replace("%timeleft%", ban.getTimeLeft())
                                    .replace("%moderator%", moderate.getAsMention())
                                    .build()))
                            .flatMap(message -> guild.ban(member, 1, TimeUnit.DAYS).reason(ban.reason())))
                    .queue();
            return;
        }

        // Check user's name
        final CoGuild coGuild = cobalt.data.getGuild(guild);
        if (coGuild.nicknameBlacklist.contains(member.getEffectiveName().toLowerCase())) member.modifyNickname(coGuild.getModeratedNickname()).queue();

        // Increase stats
        final CoGuildStats stats = cobalt.guildStats.get(guild.getIdLong());
        if (stats == null) {
            cobalt.guildStats.put(guild.getIdLong(), new CoGuildStats(guild));
            return;
        }
        stats.memberCount++;
        if (!user.isBot()) stats.humanCount++;
    }
}

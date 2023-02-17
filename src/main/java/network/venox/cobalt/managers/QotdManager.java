package network.venox.cobalt.managers;

import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import network.venox.cobalt.Cobalt;
import network.venox.cobalt.data.CoGuild;
import network.venox.cobalt.data.objects.CoQuestion;

import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.concurrent.TimeUnit;


public class QotdManager {
    @NotNull private final Cobalt cobalt;

    public QotdManager(@NotNull Cobalt cobalt) {
        this.cobalt = cobalt;
    }

    public void start() {
        // Calculate initial delay
        final ZonedDateTime now = ZonedDateTime.now(ZoneId.of("America/New_York"));
        ZonedDateTime next = now.withHour(17).withMinute(0).withSecond(0).withNano(0);
        if (now.compareTo(next) > 0) next = next.plusDays(1);

        // Schedule task
        cobalt.scheduledExecutorService.scheduleAtFixedRate(this::send, Duration.between(now, next).toMillis(), TimeUnit.DAYS.toMillis(1), TimeUnit.MILLISECONDS);
    }

    public void send() {
        final CoQuestion question = cobalt.data.global.getNextQuestion();
        if (question == null) return;
        final int count = cobalt.data.global.qotdCount;
        for (final CoGuild guild: cobalt.data.guilds) {
            final TextChannel qotdChannel = guild.getQotdChannel();
            if (qotdChannel != null) question.send(count, qotdChannel, guild.getQotdRole());
        }
        question.used++;
        cobalt.data.global.qotdCount++;

        final Role qotdManager = cobalt.config.getGuildQotdManager(null);
        final TextChannel qotdChat = cobalt.config.getGuildQotdChat();
        if (qotdManager != null && qotdChat != null && cobalt.data.global.qotds.size() == question.id) qotdChat.sendMessage(":warning: We're out of questions, resorting to backups! " + qotdManager.getAsMention()).queue();
    }
}

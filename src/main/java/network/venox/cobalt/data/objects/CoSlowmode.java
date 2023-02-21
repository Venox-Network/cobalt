package network.venox.cobalt.data.objects;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import network.venox.cobalt.data.CoObject;
import network.venox.cobalt.utility.CoMapper;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public final class CoSlowmode implements CoObject {
    public final long channel;
    public int minimum;
    public int maximum;
    @Nullable public Long lastCheck;

    public CoSlowmode(long channel, int minimum, int maximum) {
        this.channel = channel;
        this.minimum = minimum;
        this.maximum = maximum;
    }

    @Nullable
    public TextChannel getChannel(@NotNull Guild guild) {
        return guild.getTextChannelById(channel);
    }

    @Override @NotNull @Contract(" -> new")
    public Map<String, Object> toMap() {
        return Map.of(
                "minimum", minimum,
                "maximum", maximum);
    }

    public void setSlowmode(@NotNull TextChannel channel) {
        // Check if slowmode has been set recently
        if (lastCheck != null && System.currentTimeMillis() - lastCheck < 15000) return;

        // Get the users in chat sent since last check or 15 seconds ago
        int total = 0;
        final Set<Long> users = new HashSet<>();
        final OffsetDateTime time = OffsetDateTime.now().minusSeconds(lastCheck == null ? 15 : ((System.currentTimeMillis() - lastCheck) / 1000));
        for (final Message message : channel.getIterableHistory()) {
            total++;
            if (total > maximum || message.getTimeCreated().isBefore(time)) break;
            final User author = message.getAuthor();
            if (!author.isBot()) users.add(author.getIdLong());
        }

        // Update lastCheck
        lastCheck = System.currentTimeMillis();

        // Calculate slowmode
        final Integer slowmode = CoMapper.toInt(Math.max(minimum, Math.min(maximum, users.size())));
        if (slowmode == null) return;

        // Set slowmode
        channel.getManager().setSlowmode(slowmode).queue();
    }
}

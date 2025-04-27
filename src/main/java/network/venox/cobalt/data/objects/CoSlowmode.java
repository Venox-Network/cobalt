package network.venox.cobalt.data.objects;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.javautilities.manipulation.Mapper;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public final class CoSlowmode extends CoObject {
    @NotNull private final JDA jda;
    private final long guildId;

    public final long channel;
    public int minimum;
    public int maximum;
    @Nullable public Long lastCheck;

    public CoSlowmode(@NotNull JDA jda, long guildId, long channel, int minimum, int maximum) {
        this.jda = jda;
        this.guildId = guildId;
        this.channel = channel;
        this.minimum = minimum;
        this.maximum = maximum;
    }

    @Override @NotNull @Contract(" -> new")
    public Map<String, Object> toMap() {
        return Map.of(
                "minimum", minimum,
                "maximum", maximum);
    }

    @Override
    public boolean isNull() {
        return getChannel() == null;
    }

    @Nullable
    public Guild getGuild() {
        return jda.getGuildById(guildId);
    }

    @Nullable
    public TextChannel getChannel() {
        final Guild guild = getGuild();
        if (guild == null) return null;
        return guild.getTextChannelById(channel);
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
        final Integer slowmode = Mapper.toInt(Math.max(minimum, Math.min(maximum, users.size())));
        if (slowmode == null) return;

        // Set slowmode
        channel.getManager().setSlowmode(slowmode).queue();
    }
}

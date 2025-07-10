package network.venox.cobalt.mongo;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;

import network.venox.cobalt.Cobalt;

import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.ObjectId;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;


public class Server {
    @BsonId public ObjectId id;
    @BsonProperty("guild") public long guildId;
    @BsonProperty("welcome_channel") @Nullable public Long welcomeChannelId;
    @BsonProperty("mute_role") @Nullable public Long muteRoleId;
    @BsonProperty("muted_users") @Nullable public Set<Long> mutedUserIds;
    @BsonProperty("status_roles") @Nullable public Map<String, Long> statusRoles;

    @Nullable
    public Guild getGuild(@NotNull JDA jda) {
        return jda.getGuildById(guildId);
    }

    @Nullable
    public TextChannel getWelcomeChannel(@NotNull JDA jda) {
        if (welcomeChannelId == null) return null;
        final Guild guild = getGuild(jda);
        return guild == null ? null : guild.getTextChannelById(welcomeChannelId);
    }

    @Nullable
    public Role getMuteRole(@NotNull JDA jda) {
        if (muteRoleId == null) return null;
        final Guild guild = getGuild(jda);
        return guild == null ? null : guild.getRoleById(muteRoleId);
    }

    @NotNull
    public Map<String, Long> statusRoles() {
        return statusRoles == null ? Map.of() : statusRoles;
    }

    public void sendWelcomeMessage(@NotNull Cobalt cobalt, @NotNull User user) {
        final GuildMessageChannel channel = getWelcomeChannel(cobalt.jda);
        if (channel != null) cobalt.config.welcomeQuestions.stream()
                .skip(Cobalt.RANDOM.nextInt(cobalt.config.welcomeQuestions.size()))
                .findFirst()
                .ifPresent(randomQuestion -> channel.sendMessage(":wave: **Welcome, " + user.getAsMention() + "!** " + randomQuestion).queue());
    }
}

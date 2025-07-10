package network.venox.cobalt.mongo;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import network.venox.cobalt.Cobalt;

import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.ObjectId;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.lazylibrary.utility.LazyUtilities;

import java.util.*;


public class Server {
    @NotNull public static final String PROP_GUILD = "guild";
    @NotNull public static final String PROP_WELCOME_CHANNEL = "welcome_channel";
    @NotNull public static final String PROP_MUTE_ROLE = "mute_role";
    @NotNull public static final String PROP_MUTED_USERS = "muted_users";

    @BsonId public ObjectId id;
    @BsonProperty(PROP_GUILD) public long guildId;
    @BsonProperty(PROP_WELCOME_CHANNEL) @Nullable public Long welcomeChannelId;
    @BsonProperty(PROP_MUTE_ROLE) @Nullable public Long muteRoleId;
    @BsonProperty(PROP_MUTED_USERS) @Nullable public Set<Long> mutedUserIds;

    @NotNull
    public Optional<Guild> guild(@NotNull JDA jda) {
        return Optional.ofNullable(jda.getGuildById(guildId));
    }

    @NotNull
    public Optional<TextChannel> welcomeChannel(@NotNull JDA jda) {
        return welcomeChannelId != null ? guild(jda).map(value -> value.getTextChannelById(welcomeChannelId)) : Optional.empty();
    }

    @NotNull
    public Optional<Role> muteRole(@NotNull JDA jda) {
        return muteRoleId != null ? guild(jda).map(value -> value.getRoleById(muteRoleId)) : Optional.empty();
    }

    public void sendWelcomeMessage(@NotNull Cobalt bot, @NotNull User user) {
        welcomeChannel(bot.jda).ifPresent(textChannel -> textChannel.sendMessage(":wave: **Welcome " + user.getAsMention() + "!** " + bot.config.welcomeQuestions.get(LazyUtilities.RANDOM.nextInt(bot.config.welcomeQuestions.size()))).queue());
    }
}

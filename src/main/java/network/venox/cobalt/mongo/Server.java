package network.venox.cobalt.mongo;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import network.venox.cobalt.CoConfig;

import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonProperty;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.javautilities.MiscUtility;

import java.util.*;


public class Server {
    @NotNull public static final String PROP_WELCOME_CHANNEL = "welcome_channel";
    @NotNull public static final String PROP_LOCK_PRESETS = "lock_presets";
    @NotNull public static final String PROP_MUTE_ROLE = "mute_role";
    @NotNull public static final String PROP_MUTED_USERS = "muted_users";

    @BsonId public long guild;
    @BsonProperty(PROP_WELCOME_CHANNEL) @Nullable public Long welcomeChannel;
    /**
     * [preset name, role IDs]
     */
    @BsonProperty(PROP_LOCK_PRESETS) @Nullable public Map<String, Set<Long>> lockPresets;
    @BsonProperty(PROP_MUTE_ROLE) @Nullable public Long muteRole;
    @BsonProperty(PROP_MUTED_USERS) @Nullable public Set<Long> mutedUsers;

    @NotNull
    public Optional<Guild> guild(@NotNull JDA jda) {
        return Optional.ofNullable(jda.getGuildById(guild));
    }

    @NotNull
    public Optional<TextChannel> welcomeChannel(@NotNull JDA jda) {
        return welcomeChannel != null ? guild(jda).map(value -> value.getTextChannelById(welcomeChannel)) : Optional.empty();
    }

    @NotNull
    public Optional<Role> muteRole(@NotNull JDA jda) {
        return muteRole != null ? guild(jda).map(value -> value.getRoleById(muteRole)) : Optional.empty();
    }

    @NotNull
    public Set<Long> mutedUsers() {
        return Objects.requireNonNullElse(mutedUsers, Collections.emptySet());
    }

    public void sendWelcomeMessage(@NotNull CoConfig config, @NotNull User user) {
        welcomeChannel(user.getJDA()).ifPresent(textChannel -> textChannel.sendMessage(":wave: **Welcome " + user.getAsMention() + "!** " + config.welcomeQuestions.get(MiscUtility.RANDOM.nextInt(config.welcomeQuestions.size()))).queue());
    }
}

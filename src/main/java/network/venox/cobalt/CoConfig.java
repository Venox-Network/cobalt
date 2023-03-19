package network.venox.cobalt;

import com.freya02.botcommands.api.application.slash.GlobalSlashEvent;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;

import network.venox.cobalt.data.objects.CoEmbed;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.yaml.NodeStyle;

import java.util.*;
import java.util.stream.Collectors;


public class CoConfig {
    private JDA jda;
    @NotNull public final CoFile file = new CoFile("config", NodeStyle.BLOCK, true);

    @Nullable public final String token = file.yaml.node("token").getString();
    @Nullable public final String database = file.yaml.node("database").getString();
    @NotNull public final List<Long> owners;

    // GUILD
    @NotNull private final ConfigurationNode guildNode = file.yaml.node("guild");
    public final long guildId = guildNode.node("id").getLong();
    @Nullable public final String guildInvite = guildNode.node("invite").getString();
    public final long guildQotdManager = guildNode.node("qotd-manager").getLong();
    public final long guildQotdChat = guildNode.node("qotd-chat").getLong();
    public final long guildMod = guildNode.node("mod").getLong();
    public final long guildModmail = guildNode.node("modmail").getLong();
    public final long guildLog = guildNode.node("log").getLong();

    // STATUSES
    @NotNull public Set<Activity> statuses = new HashSet<>();

    public CoConfig() {
        // owners
        List<Long> newOwners = null;
        try {
            newOwners = file.yaml.node("owners").getList(Long.class);
        } catch (final SerializationException e) {
            e.printStackTrace();
        }
        owners = newOwners == null ? Collections.emptyList() : newOwners;
    }

    public void loadJda(@NotNull Cobalt cobalt) {
        this.jda = cobalt.jda;

        // statuses
        final String servers = String.valueOf(cobalt.guildStats.size());
        final String users = String.valueOf(cobalt.guildStats.values().stream()
                .mapToInt(guildStats -> guildStats.memberCount)
                .sum());
        this.statuses = file.yaml.node("statuses").childrenList().stream()
                .map(node -> {
                    final String statusString = node.node("status").getString();
                    final String typeString = node.node("type").getString();
                    if (statusString == null || typeString == null) return null;
                    return Activity.of(Activity.ActivityType.valueOf(typeString.toUpperCase()), statusString
                            .replace("%servers%", servers)
                            .replace("%users%", users));
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    @Nullable
    public Guild getGuild() {
        return jda.getGuildById(guildId);
    }

    @Nullable
    public Role getGuildQotdManager(@Nullable Guild guild) {
        if (guild == null) {
            guild = getGuild();
            if (guild == null) return null;
        }
        return guild.getRoleById(guildQotdManager);
    }

    @Nullable
    public TextChannel getGuildQotdChat() {
        final Guild guild = getGuild();
        if (guild == null) return null;
        return guild.getTextChannelById(guildQotdChat);
    }

    @Nullable
    public Role getGuildMod() {
        final Guild guild = getGuild();
        if (guild == null) return null;
        return guild.getRoleById(guildMod);
    }

    @Nullable
    public ForumChannel getGuildModmail() {
        final Guild guild = getGuild();
        if (guild == null) return null;
        return guild.getForumChannelById(guildModmail);
    }

    @Nullable
    public TextChannel getGuildLog() {
        final Guild guild = getGuild();
        if (guild == null) return null;
        return guild.getTextChannelById(guildLog);
    }

    public void sendLog(@NotNull String title, @NotNull String message) {
        final TextChannel log = getGuildLog();
        if (log != null) log.sendMessage("**`     " + title.toUpperCase() + "     `**\n" + message + "\n**`     " + title.toUpperCase() + "     `**").setAllowedMentions(List.of()).queue();
    }

    public boolean isOwner(@NotNull User user) {
        return owners.contains(user.getIdLong());
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean checkIsOwner(@NotNull GenericCommandInteractionEvent event) {
        final boolean isOwner = isOwner(event.getUser());
        if (!isOwner) event.replyEmbeds(CoEmbed.NO_PERMISSION.build()).setEphemeral(true).queue();
        return isOwner;
    }

    public boolean isQotdManager(@NotNull User user) {
        final Guild guild = getGuild();
        if (guild == null) return false;
        final Member member = guild.getMember(user);
        final Role qotdManager = getGuildQotdManager(guild);
        return member != null && qotdManager != null && member.getRoles().contains(qotdManager);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean checkIsQotdManager(@NotNull GlobalSlashEvent event) {
        final boolean isQotdManager = isQotdManager(event.getUser());
        if (!isQotdManager) event.replyEmbeds(CoEmbed.NO_PERMISSION.build()).setEphemeral(true).queue();
        return isQotdManager;
    }
}

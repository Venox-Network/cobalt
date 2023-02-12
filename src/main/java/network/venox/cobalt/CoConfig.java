package network.venox.cobalt;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.yaml.NodeStyle;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;


public class CoConfig {
    private JDA jda;
    @NotNull public final CoFile file = new CoFile("config", NodeStyle.BLOCK, true);

    @Nullable public final String token = file.yaml.node("token").getString();
    @NotNull public final Set<Long> owners = file.yaml.node("owners").childrenList().stream()
                .map(ConfigurationNode::getLong)
                .collect(Collectors.toSet());

    // GUILD
    @NotNull private final ConfigurationNode guildNode = file.yaml.node("guild");
    public final long guildId = guildNode.node("id").getLong();
    @Nullable public final String guildInvite = guildNode.node("invite").getString();
    public final long guildQotdManager = guildNode.node("qotd-manager").getLong();
    public final long guildQotdChat = guildNode.node("qotd-chat").getLong();
    public final long guildLog = guildNode.node("log").getLong();

    // STATUSES
    @NotNull public Set<Activity> statuses = new HashSet<>();

    public void loadJda(@NotNull JDA jda) {
        this.jda = jda;

        // statuses
        final List<Guild> guilds = jda.getGuilds();
        final String servers = String.valueOf(guilds.size());
        final String users = String.valueOf(guilds.stream()
                .mapToInt(guild -> guild.loadMembers().get().size())
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
    public Role getGuildQotdManager() {
        final Guild guild = getGuild();
        if (guild == null) return null;
        return guild.getRoleById(guildQotdManager);
    }

    @Nullable
    public TextChannel getGuildQotdChat() {
        final Guild guild = getGuild();
        if (guild == null) return null;
        return guild.getTextChannelById(guildQotdChat);
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
}

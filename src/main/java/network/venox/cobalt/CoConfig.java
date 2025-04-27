package network.venox.cobalt;

import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import org.spongepowered.configurate.ConfigurationNode;

import xyz.srnyx.lazylibrary.LazyEmbed;
import xyz.srnyx.lazylibrary.config.LazyChannel;
import xyz.srnyx.lazylibrary.config.LazyRole;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;


public class CoConfig {
    @NotNull private final Cobalt bot;

    @NotNull public final GuildNode guild;
    @Nullable public List<Activity> statuses;
    @NotNull public final Set<String> welcomeQuestions;
    @NotNull public final Set<String> qotwSimilarityIgnored;

    public CoConfig(@NotNull Cobalt bot) {
        this.bot = bot;
        guild = new GuildNode(bot.settings.fileSettings.file.yaml.node("guild"));
        welcomeQuestions = bot.settings.fileSettings.file.yaml.node("welcome-questions").childrenList().stream()
                .map(ConfigurationNode::getString)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        qotwSimilarityIgnored = bot.settings.fileSettings.file.yaml.node("qotw-similarity-ignored").childrenList().stream()
                .map(ConfigurationNode::getString)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    public void loadStatuses() {
        final String servers = String.valueOf(bot.dataManager.guildStats.size());
        final String users = String.valueOf(bot.dataManager.guildStats.values().stream()
                .mapToInt(DataManager.GuildStats::memberCount)
                .sum());
        statuses = bot.settings.fileSettings.file.yaml.node("statuses").childrenList().stream()
                .map(node -> {
                    // Get status
                    final String status = node.getString();
                    if (status == null) return null;

                    // Get type
                    Activity.ActivityType type = Activity.ActivityType.CUSTOM_STATUS;
                    if (status.startsWith("watching")) {
                        type = Activity.ActivityType.WATCHING;
                    } else if (status.startsWith("listening")) {
                        type = Activity.ActivityType.LISTENING;
                    } else if (status.startsWith("playing")) {
                        type = Activity.ActivityType.PLAYING;
                    }

                    // Get Activity
                    return Activity.of(type, status
                            .replace("%servers%", servers)
                            .replace("%users%", users));
                })
                .filter(Objects::nonNull)
                .toList();
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean checkIsOwner(@NotNull GenericCommandInteractionEvent event) {
        final boolean isOwner = bot.isOwner(event.getUser().getIdLong());
        if (!isOwner) event.replyEmbeds(LazyEmbed.noPermission().build(bot)).setEphemeral(true).queue();
        return isOwner;
    }

    public class GuildNode implements Supplier<Guild> {
        public final long id;
        @Nullable public final String invite;
        @NotNull public final LazyRole botManager;
        @NotNull public final LazyChannel<GuildMessageChannel> botManagerChat;
        @NotNull public final LazyRole mod;
        @NotNull public final LazyChannel<ForumChannel> modmail;
        @NotNull public final LazyChannel<GuildMessageChannel> log;

        public GuildNode(@NotNull ConfigurationNode node) {
            this.id = node.node("id").getLong();
            this.invite = node.node("invite").getString();
            this.botManager = new LazyRole(bot, this, node.node("bot-manager"));
            this.botManagerChat = new LazyChannel<>(this, node.node("bot-manager-chat"));
            this.mod = new LazyRole(bot, this, node.node("mod"));
            this.modmail = new LazyChannel<>(this, node.node("modmail"));
            this.log = new LazyChannel<>(this, node.node("log"));
        }

        @Override
        public Guild get() {
            return getGuild();
        }

        @NotNull
        public Guild getGuild() {
            return Objects.requireNonNull(bot.jda.getGuildById(id));
        }

        public void sendLog(@NotNull String title, @NotNull String message) {
            log.getChannel().ifPresent(channel -> channel.sendMessage("**`     " + title.toUpperCase() + "     `**\n" + message + "\n**`     " + title.toUpperCase() + "     `**").setAllowedMentions(Set.of()).queue());
        }
    }
}

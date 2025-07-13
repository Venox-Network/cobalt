package network.venox.cobalt;

import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import org.spongepowered.configurate.ConfigurationNode;

import xyz.srnyx.lazylibrary.LazyEmbed;
import xyz.srnyx.lazylibrary.config.LazyChannel;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;


public class CoConfig {
    @NotNull private final Cobalt bot;

    @NotNull public final GuildNode guild;
    @Nullable public List<Activity> statuses;
    @NotNull public final List<String> welcomeQuestions;

    public CoConfig(@NotNull Cobalt bot) {
        this.bot = bot;
        guild = new GuildNode(bot.settings.fileSettings.file.yaml.node("guild"));
        welcomeQuestions = bot.settings.fileSettings.file.yaml.node("welcome-questions").childrenList().stream()
                .map(ConfigurationNode::getString)
                .filter(Objects::nonNull)
                .toList();
    }

    public void loadStatuses() {
        final String guilds = String.valueOf(bot.jda.getGuilds().size());
        final String totalMembers = String.valueOf(bot.dataManager.totalMembers);
        final String uniqueMembers = String.valueOf(bot.dataManager.uniqueMembers);
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
                            .replace("%servers%", guilds)
                            .replace("%users%", totalMembers)
                            .replace("%unique_users%", uniqueMembers));
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
        @NotNull public final LazyChannel<GuildMessageChannel> log;

        public GuildNode(@NotNull ConfigurationNode node) {
            this.id = node.node("id").getLong();
            this.log = new LazyChannel<>(this, node.node("log"));
        }

        @Override
        public Guild get() {
            return Objects.requireNonNull(bot.jda.getGuildById(id));
        }

        public void sendLog(@NotNull String title, @NotNull String message) {
            log.getChannel().ifPresent(channel -> channel.sendMessage("**`     " + title.toUpperCase() + "     `**\n" + message + "\n**`     " + title.toUpperCase() + "     `**").setAllowedMentions(Set.of()).queue());
        }
    }
}

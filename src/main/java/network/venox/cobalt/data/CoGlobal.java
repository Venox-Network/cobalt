package network.venox.cobalt.data;

import com.freya02.botcommands.api.components.Components;

import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.entities.channel.middleman.StandardGuildMessageChannel;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.CacheRestAction;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;

import network.venox.cobalt.Cobalt;
import network.venox.cobalt.data.objects.CoSuperBan;
import network.venox.cobalt.data.objects.CoQuestion;
import network.venox.cobalt.data.objects.CoWarning;
import network.venox.cobalt.CoFile;
import network.venox.cobalt.utility.CoMapper;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.yaml.NodeStyle;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


public class CoGlobal {
    @NotNull private final Cobalt cobalt;
    @NotNull public final CoFile file = new CoFile("data/global", NodeStyle.FLOW, false);

    @NotNull public final Map<Long, Long> modmailThreads = new HashMap<>();
    @NotNull public final List<CoWarning> warnings = new ArrayList<>();
    @NotNull public final Set<CoSuperBan> superBans = new HashSet<>();
    @NotNull public final List<CoQuestion> qotds = new ArrayList<>();
    public int qotdCount = 1;
    @Nullable public Activity activity = null;

    public CoGlobal(@NotNull Cobalt cobalt) {
        this.cobalt = cobalt;
        load();
    }

    public void load() {
        // modmailThreads
        for (final ConfigurationNode node : file.yaml.node("modmail-threads").childrenMap().values()) {
            final Long id = CoMapper.toLong(node.key());
            final long channelId = node.getLong();
            if (id != null && channelId != 0) modmailThreads.put(id, channelId);
        }

        // warnings
        for (final ConfigurationNode node : file.yaml.node("warnings").childrenMap().values()) {
            final Integer id = CoMapper.toInt(node.key());
            final String reason = node.node("reason").getString();
            if (id == null || reason == null) continue;
            final CoWarning warning = new CoWarning(cobalt.jda, id, node.node("user").getLong(), reason, node.node("moderator").getLong());
            if (warning.getUser().complete() != null && warning.getModerator().complete() != null) warnings.add(warning);
        }

        // superBans
        for (final ConfigurationNode node : file.yaml.node("super-bans").childrenMap().values()) {
            final Long id = CoMapper.toLong(node.key());
            final String reason = node.node("reason").getString();
            if (id == null || reason == null) continue;
            final CoSuperBan superBan = new CoSuperBan(cobalt.jda, id, reason, node.node("time").getLong(), node.node("moderator").getLong());
            if (!superBan.isExpired()) superBans.add(superBan);
        }

        // qotds
        for (final ConfigurationNode node : file.yaml.node("qotds").childrenMap().values()) {
            final Integer id = CoMapper.toInt(node.key());
            final String questionText = node.node("question").getString();
            if (id == null || questionText == null) continue;
            final CoQuestion question = new CoQuestion(cobalt.jda, id, questionText, node.node("user").getLong(), node.node("used").getInt());
            question.getUser().queue(s -> qotds.add(question), f -> {});
        }

        // qotdCount
        qotdCount = file.yaml.node("qotd-count").getInt(1);

        // activity
        final ConfigurationNode activityNode = file.yaml.node("activity");
        if (!activityNode.empty()) {
            final String type = activityNode.node("type").getString();
            final String text = activityNode.node("status").getString();
            if (type != null && text != null) activity = Activity.of(Activity.ActivityType.valueOf(type), text);
        }
    }

    public void save() throws SerializationException {
        // modmailThreads
        final ConfigurationNode modmailThreadsNode = file.yaml.node("modmail-threads");
        modmailThreadsNode.set(null);
        for (final Map.Entry<Long, Long> entry : modmailThreads.entrySet()) modmailThreadsNode.node(entry.getKey().toString()).set(entry.getValue());

        // warnings
        file.yaml.node("warnings").set(warnings.isEmpty() ? null : warnings.stream()
                .collect(Collectors.toMap(CoWarning::id, CoWarning::toMap)));

        // superBans
        final ConfigurationNode superBansNode = file.yaml.node("super-bans");
        superBansNode.set(null);
        for (final CoSuperBan ban : new HashSet<>(superBans)) {
            if (ban.isExpired()) {
                superBans.remove(ban);
                continue;
            }
            superBansNode.node(ban.user()).set(ban.toMap());
        }

        // qotds
        file.yaml.node("qotds").set(qotds.isEmpty() ? null : qotds.stream()
                .collect(Collectors.toMap(qotd -> qotd.id, CoQuestion::toMap)));

        // qotdCount
        file.yaml.node("qotd-count").set(qotdCount == 1 ? null : qotdCount);

        // activity
        final ConfigurationNode activityNode = file.yaml.node("activity");
        activityNode.set(null);
        if (activity != null) {
            activityNode.node("type").set(activity.getType().name());
            activityNode.node("status").set(activity.getName());
        }

        // SAVE FILE
        file.save();
    }

    @Nullable
    public CacheRestAction<User> getModmailUser(long channelId) {
        final Long userId = modmailThreads.entrySet().stream()
                .filter(entry -> entry.getValue() == channelId)
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
        if (userId == null) return null;
        return cobalt.jda.retrieveUserById(userId);
    }

    @Nullable
    public ThreadChannel getModmailThread(long userId) {
        final Guild guild = cobalt.config.getGuild();
        final Long threadId = modmailThreads.get(userId);
        if (guild == null || threadId == null) return null;
        final ThreadChannel thread = guild.getThreadChannelById(threadId);
        if (thread == null || thread.isArchived()) return null;
        return thread;
    }

    @Nullable
    public CoWarning getWarning(int id) {
        return warnings.stream()
                .filter(warning -> warning.id() == id)
                .findFirst()
                .orElse(null);
    }

    @NotNull
    public List<CoWarning> getWarnings(long id) {
        return warnings.stream()
                .filter(warning -> warning.user() == id)
                .toList();
    }

    public int getNextWarningId() {
        return warnings.stream()
                .mapToInt(CoWarning::id)
                .max()
                .orElse(0) + 1;
    }

    @Nullable
    public CoSuperBan getSuperBan(long id) {
        return superBans.stream()
                .filter(ban -> ban.user() == id)
                .findFirst()
                .orElse(null);
    }

    @Nullable
    public CoQuestion getQuestion(int id) {
        return qotds.stream()
                .filter(question -> question.id == id)
                .findFirst()
                .orElse(null);
    }

    public int getNextQotdId() {
        return qotds.stream()
                .mapToInt(qotd -> qotd.id)
                .max()
                .orElse(0) + 1;
    }

    public void startQotd() {
        // Calculate initial delay
        final ZonedDateTime now = ZonedDateTime.now(ZoneId.of("America/New_York"));
        ZonedDateTime next = now.withHour(17).withMinute(0).withSecond(0).withNano(0);
        if (now.compareTo(next) > 0) next = next.plusDays(1);

        // Schedule task
        cobalt.scheduledExecutorService.scheduleAtFixedRate(() -> {
            final CoQuestion question = qotds.stream()
                .min(Comparator.comparingInt(qotd -> qotd.used))
                .orElse(null);
            if (question == null) return;
            final int count = cobalt.data.global.qotdCount;
            for (final CoGuild guild: cobalt.data.guilds) {
                final StandardGuildMessageChannel qotdChannel = guild.getQotdChannel();
                if (qotdChannel != null) question.send(count, qotdChannel, guild.getQotdRole());
            }
            question.used++;
            cobalt.data.global.qotdCount++;

            final Role qotdManager = cobalt.config.getGuildQotdManager(null);
            final TextChannel qotdChat = cobalt.config.getGuildQotdChat();
            if (qotdManager != null && qotdChat != null && cobalt.data.global.qotds.size() == question.id) qotdChat.sendMessage(":warning: We're out of questions, resorting to backups! " + qotdManager.getAsMention()).queue();
        }, Duration.between(now, next).toMillis(), TimeUnit.DAYS.toMillis(1), TimeUnit.MILLISECONDS);
    }

    @NotNull
    public RestAction<Message> sendModmailConfirmation(@NotNull User author, @NotNull MessageCreateBuilder builder) {
        final long authorId = author.getIdLong();
        final MessageCreateBuilder confirmation = new MessageCreateBuilder();
        confirmation.setContent("You are about to open a new modmail thread with the Venox Network staff.\nAre you sure you want to continue?");
        confirmation.addActionRow(
                Components.successButton(buttonEvent -> {
                    final ForumChannel modmail = cobalt.config.getGuildModmail();
                    if (modmail == null) {
                        buttonEvent.reply("**ERROR:** Modmail is not configured!").setEphemeral(true).queue();
                        return;
                    }
                    final User buttonAuthor = buttonEvent.getUser();
                    final String mutualGuilds = buttonEvent.getJDA().getMutualGuilds(buttonAuthor).stream()
                            .map(Guild::getName)
                            .collect(Collectors.joining("\n- "));
                    builder.setContent("**User:** " + buttonAuthor.getAsMention() + " `" + buttonAuthor.getId() + "`\n**Mutual servers:** \n- " + mutualGuilds + "\n**Message:**\n" + builder.getContent());
                    modmail.createForumPost(buttonAuthor.getAsTag(), MessageCreateData.fromContent("Getting the mods!"))
                            .flatMap(newThread -> {
                                cobalt.data.global.modmailThreads.put(authorId, newThread.getThreadChannel().getIdLong());
                                return newThread.getMessage().editMessage("<@&" + cobalt.config.guildMod + ">");
                            })
                            .flatMap(message -> message.editMessage(MessageEditBuilder.fromCreateData(builder.build()).build()))
                            .flatMap(s -> buttonEvent.getMessage().editMessage("**Modmail thread created!** Any messages you send here will now be sent to the moderators.").setComponents(List.of()))
                            .queue();
                }).build("Yes"),
                Components.dangerButton(buttonEvent -> buttonEvent.getMessage().delete().queue()).build("No"));
        return author.openPrivateChannel().flatMap(privateChannel -> privateChannel.sendMessage(confirmation.build()));
    }
}

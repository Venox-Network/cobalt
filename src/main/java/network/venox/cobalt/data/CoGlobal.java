package network.venox.cobalt.data;

import net.dv8tion.jda.api.JDA;

import net.dv8tion.jda.api.entities.Activity;
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

import java.util.*;
import java.util.stream.Collectors;


public class CoGlobal {
    @NotNull private final JDA jda;
    @NotNull public final CoFile file = new CoFile("data/global", NodeStyle.FLOW, false);

    @NotNull public final List<CoWarning> warnings = new ArrayList<>();
    @NotNull public final Set<CoSuperBan> superBans = new HashSet<>();
    @NotNull public final List<CoQuestion> qotds = new ArrayList<>();
    public int qotdCount = 1;
    @Nullable public Activity activity = null;

    public CoGlobal(@NotNull JDA jda) {
        this.jda = jda;
        load();
    }

    public void load() {
        // warnings
        for (final ConfigurationNode node : file.yaml.node("warnings").childrenMap().values()) {
            final Integer id = CoMapper.toInt(node.key());
            final String reason = node.node("reason").getString();
            if (id == null || reason == null) continue;
            final CoWarning warning = new CoWarning(id, node.node("user").getLong(), reason, node.node("moderator").getLong());
            if (warning.getUser(jda) != null && warning.getModerator(jda) != null) warnings.add(warning);
        }

        // superBans
        for (final ConfigurationNode node : file.yaml.node("super-bans").childrenMap().values()) {
            final Long id = CoMapper.toLong(node.key());
            final String reason = node.node("reason").getString();
            if (id == null || reason == null) continue;
            final CoSuperBan superBan = new CoSuperBan(id, reason, node.node("time").getLong(), node.node("moderator").getLong());
            if (!superBan.isExpired()) superBans.add(superBan);
        }

        // qotds
        for (final ConfigurationNode node : file.yaml.node("qotds").childrenMap().values()) {
            final Integer id = CoMapper.toInt(node.key());
            final String questionText = node.node("question").getString();
            if (id == null || questionText == null) continue;
            final CoQuestion question = new CoQuestion(id, questionText, node.node("user").getLong(), node.node("used").getInt());
            if (question.getUser(jda) != null) qotds.add(question);
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

    @Nullable
    public CoQuestion getNextQuestion() {
        return qotds.stream()
                .min(Comparator.comparingInt(qotd -> qotd.used))
                .orElse(null);
    }
}

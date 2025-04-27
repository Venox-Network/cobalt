package network.venox.cobalt;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.StandardGuildMessageChannel;

import network.venox.cobalt.mongo.*;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import xyz.srnyx.lazylibrary.LazyEmoji;

import xyz.srnyx.magicmongo.MagicDatabase;
import xyz.srnyx.magicmongo.SingleMongo;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;


public class DataManager {
    @NotNull private final Cobalt bot;
    @NotNull public final MagicDatabase mongo;
    @NotNull public final Map<Long, GuildStats> guildStats = new HashMap<>();

    public DataManager(@NotNull Cobalt bot) {
        this.bot = bot;
        final String url = bot.settings.fileSettings.file.yaml.node("mongo").getString();
        if (url == null) throw new IllegalArgumentException("MongoDB URL not found in config!");
        mongo = new SingleMongo(url).database.loadMagicCollections(Map.of(
                "bans", Ban.class,
                "misc", Global.class,
                "modmails", Modmail.class,
                "questions", Question.class,
                "servers", Server.class,
                "warnings", Warning.class));

        // Get guild stats
        for (final Guild guild : bot.jda.getGuilds()) guild.loadMembers().onSuccess(members -> {
            final int memberCount = members.size();
            final int humanCount = (int) members.stream()
                    .filter(member -> !member.getUser().isBot())
                    .count();
            guildStats.put(guild.getIdLong(), new GuildStats(memberCount, humanCount));
        });
    }

    public int getNextQotwId() {
        final Question question = mongo.getMagicCollection(Question.class).findMany(Filters.exists("question_id")).stream()
                .max(Comparator.comparingInt(qotw -> qotw.questionId))
                .orElse(null);
        return question == null ? 1 : question.questionId + 1;
    }

    @Nullable
    public Question getNextQuestion() {
        return mongo.getMagicCollection(Question.class).findMany(Filters.exists("question_id")).stream()
                .min(Comparator.comparingInt(qotw -> qotw.used))
                .orElse(null);
    }

    /**
     * Every Sunday at 5:00 PM Easter Time
     */
    public void startQotw() {
        final JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("DataManager", this);
        try {
            final Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
            scheduler.scheduleJob(
                    JobBuilder.newJob(QotwJob.class)
                            .usingJobData(jobDataMap)
                            .withIdentity("qotwJob", "cobalt")
                            .build(),
                    TriggerBuilder.newTrigger()
                            .withIdentity("qotwTrigger", "cobalt")
                            .withSchedule(CronScheduleBuilder.cronSchedule("0 0 17 ? * SUN *"))
                            .build());
            scheduler.start();
        } catch (final SchedulerException e) {
            e.printStackTrace();
        }
    }

    public static class QotwJob implements Job {
        @Override
        public void execute(@NotNull JobExecutionContext context) {
            final DataManager dataManager = (DataManager) context.getJobDetail().getJobDataMap().get("DataManager");

            // Post question
            final Question question = dataManager.getNextQuestion();
            if (question == null) return;
            for (final Server server : dataManager.mongo.getMagicCollection(Server.class).collection.find()) {
                final StandardGuildMessageChannel qotwChannel = server.getQotwChannel(dataManager.bot.jda);
                if (qotwChannel != null) question.send(dataManager.getQotwCount(), qotwChannel, server.getQotwRole(dataManager.bot.jda));
            }

            // Update used count and QOTW count
            dataManager.mongo.getMagicCollection(Question.class).updateOne(Filters.eq("question_id", question.questionId), Updates.inc("used", 1));
            dataManager.mongo.getMagicCollection(Global.class).updateOne(Filters.exists("qotw_count"), Updates.inc("qotw_count", 1));

            // Send warning to Bot Managers if we're out of questions
            final TextChannel botManagerChat = dataManager.bot.config.getGuildBotManagerChat();
            final Question nextQuestion = dataManager.getNextQuestion();
            if (botManagerChat != null && nextQuestion != null && nextQuestion.questionId <= question.questionId) botManagerChat.sendMessage(LazyEmoji.WARNING + " We're out of questions, resorting to backups! <@&" + dataManager.bot.config.guildBotManager + ">").queue();
        }
    }

    public record GuildStats(int memberCount, int humanCount) {}
}

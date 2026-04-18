package network.venox.cobalt.commands.guild.survey;

import com.mongodb.client.model.Filters;

import io.github.freya022.botcommands.api.commands.annotations.Command;
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashOption;

import network.venox.cobalt.CoUtility;
import network.venox.cobalt.MongoProvider;
import network.venox.cobalt.mongo.Survey;

import org.bson.types.ObjectId;

import org.jetbrains.annotations.NotNull;

import xyz.srnyx.lazylibrary.LazyEmbed;
import xyz.srnyx.lazylibrary.emoji.LazyEmoji;


@Command
public class SurveyEdit {
    @NotNull private final MongoProvider mongo;
    @NotNull private final SurveyCommon surveyCommon;

    public SurveyEdit(@NotNull MongoProvider mongo, @NotNull SurveyCommon surveyCommon) {
        this.mongo = mongo;
        this.surveyCommon = surveyCommon;
    }

    @JDASlashCommand(
            name = "survey",
            subcommand = "edit",
            description = "Edit an existing survey")
    public void surveyEdit(@NotNull GuildSlashEvent event,
                           @SlashOption(description = "The ID of the survey to edit", autocomplete = SurveyCommon.AC_ID) @NotNull String id) {
        // Check permissions
        if (!event.getMember().hasPermission(SurveyCommon.REQUIRED_PERMISSION)) {
            event.reply(LazyEmoji.NO + " You don't have permission to edit surveys!").setEphemeral(true).queue();
            return;
        }

        // Get ID as ObjectId
        final ObjectId objectId = CoUtility.toObjectId(id).orElse(null);
        if (objectId == null) {
            event.replyEmbeds(LazyEmbed.invalidArgument("id", id, "Not a valid ObjectId").build()).setEphemeral(true).queue();
            return;
        }

        // Get Survey
        final Survey survey = mongo.database.getMagicCollection(Survey.class)
                .findOne(Filters.and(
                        Filters.eq(Survey.PROP_GUILD, event.getGuild().getIdLong()),
                        Filters.eq("_id", objectId)))
                .orElse(null);
        if (survey == null) {
            event.reply(LazyEmoji.NO + " Survey with ID `" + id + "` not found!").setEphemeral(true).queue();
            return;
        }

        // Reply with builder
        event.reply(surveyCommon.getBuilder(survey)).useComponentsV2().setEphemeral(true).queue();
    }
}

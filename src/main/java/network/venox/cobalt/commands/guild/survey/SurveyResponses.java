package network.venox.cobalt.commands.guild.survey;

import com.mongodb.client.model.Filters;

import io.github.freya022.botcommands.api.commands.annotations.Command;
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashOption;

import net.dv8tion.jda.api.components.container.Container;
import net.dv8tion.jda.api.components.replacer.ComponentReplacer;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;

import network.venox.cobalt.CoUtility;
import network.venox.cobalt.MongoProvider;
import network.venox.cobalt.mongo.Survey;
import network.venox.cobalt.mongo.SurveyResponse;

import org.bson.types.ObjectId;

import org.jetbrains.annotations.NotNull;

import xyz.srnyx.javautilities.manipulation.Mapper;

import xyz.srnyx.lazylibrary.LazyEmbed;
import xyz.srnyx.lazylibrary.emoji.LazyEmoji;
import xyz.srnyx.lazylibrary.paginator.PaginatorV2;
import xyz.srnyx.lazylibrary.paginator.PaginatorsV2;

import java.util.ArrayList;
import java.util.List;


@Command
public class SurveyResponses {
    private static final int ID_RESPONSE = 5266426;

    @NotNull private final MongoProvider mongo;
    @NotNull private final PaginatorsV2 paginators;

    public SurveyResponses(@NotNull MongoProvider mongo, @NotNull PaginatorsV2 paginators) {
        this.mongo = mongo;
        this.paginators = paginators;
    }

    @JDASlashCommand(
            name = "survey",
            subcommand = "responses",
            description = "View the responses of a survey")
    public void surveyResponses(@NotNull GuildSlashEvent event,
                                @SlashOption(description = "The ID of the survey to view responses of", autocomplete = SurveyCommon.AC_ID) @NotNull String id) {
        // Check permissions
        if (!event.getMember().hasPermission(SurveyCommon.REQUIRED_PERMISSION)) {
            event.reply(LazyEmoji.NO + " You don't have permission to view survey responses!").setEphemeral(true).queue();
            return;
        }

        // Get ObjectId
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

        // Get responses
        final List<SurveyResponse> responses = mongo.database.getMagicCollection(SurveyResponse.class).findMany(Filters.eq(SurveyResponse.PROP_SURVEY, survey.id));
        if (responses.isEmpty()) {
            event.reply(LazyEmoji.NO + " No responses for survey **" + survey.name + "** yet!").setEphemeral(true).queue();
            return;
        }

        // Sort newest first
        responses.sort((a, b) -> b.created.compareTo(a.created));

        // Build Containers
        final List<Container> containers = new ArrayList<>();
        for (final SurveyResponse response : responses) containers.add(response.toContainer().withAccentColor(Mapper.toColor(response.user)).withUniqueId(ID_RESPONSE));

        // Send paginator
        final PaginatorV2 paginator = paginators.createPaginator(
                containers.size(),
                data -> ComponentReplacer.byUniqueId(ID_RESPONSE, containers.get(data.currentPage)));
        event.replyComponents(
                        containers.getFirst(),
                        Container.of(
                                        TextDisplay.of("## " + survey.name + "\n**Responses:** " + responses.size() + "\n**Status:** " + (survey.open ? "Open" : "Closed")),
                                        paginator.getButtonRow())
                                .withAccentColor(survey.color()))
                .useComponentsV2().setEphemeral(true).queue();
    }
}

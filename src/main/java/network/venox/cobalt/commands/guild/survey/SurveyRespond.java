package network.venox.cobalt.commands.guild.survey;

import com.mongodb.client.model.Filters;

import io.github.freya022.botcommands.api.commands.annotations.Command;
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashOption;
import io.github.freya022.botcommands.api.commands.application.slash.autocomplete.annotations.AutocompleteHandler;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command.Choice;

import network.venox.cobalt.MongoProvider;
import network.venox.cobalt.mongo.Survey;

import org.bson.types.ObjectId;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.javautilities.MiscUtility;

import xyz.srnyx.lazylibrary.LazyEmbed;

import java.util.ArrayList;
import java.util.List;


@Command
public class SurveyRespond {
    @NotNull private static final String AC_ID_OPEN = "SurveyRespond.ac.id.open";

    @NotNull private final MongoProvider mongo;
    @NotNull private final SurveyCommon surveyCommon;

    public SurveyRespond(@NotNull MongoProvider mongo, @NotNull SurveyCommon surveyCommon) {
        this.mongo = mongo;
        this.surveyCommon = surveyCommon;
    }

    @JDASlashCommand(
            name = "survey",
            subcommand = "respond",
            description = "Respond to an open survey")
    public void surveyRespond(@NotNull GuildSlashEvent event,
                              @SlashOption(description = "The ID of the survey to respond to", autocomplete = AC_ID_OPEN) @NotNull String id) {
        // Get ObjectId
        final ObjectId objectId = MiscUtility.handleException(() -> new ObjectId(id)).orElse(null);
        if (objectId == null) {
            event.replyEmbeds(LazyEmbed.invalidArgument("id", id, "Not a valid ObjectId").build()).setEphemeral(true).queue();
            return;
        }

        // Get Survey
        final Survey survey = mongo.database.getMagicCollection(Survey.class)
                .findOne(Filters.and(
                        Filters.eq(Survey.PROP_GUILD, event.getGuild().getIdLong()),
                        Filters.eq("_id", objectId),
                        Filters.eq(Survey.PROP_OPEN, true)))
                .orElse(null);
        if (survey == null) {
            event.reply("Survey with ID `" + id + "` not found or is not open!").setEphemeral(true).queue();
            return;
        }

        // Reply with modal
        surveyCommon.respond(event, survey);
    }

    @AutocompleteHandler(AC_ID_OPEN) @NotNull
    public List<Choice> acIdOpen(@NotNull CommandAutoCompleteInteractionEvent event) {
        final Guild guild = event.getGuild();
        if (guild == null) return List.of();
        return mongo.database.getMagicCollection(Survey.class)
                .find(Filters.and(
                        Filters.eq(Survey.PROP_GUILD, guild.getIdLong()),
                        Filters.eq(Survey.PROP_OPEN, true)))
                .map(Survey::toCommandChoice)
                .into(new ArrayList<>());
    }
}

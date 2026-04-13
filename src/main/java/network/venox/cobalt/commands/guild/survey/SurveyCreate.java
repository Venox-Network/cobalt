package network.venox.cobalt.commands.guild.survey;

import io.github.freya022.botcommands.api.commands.annotations.Command;
import io.github.freya022.botcommands.api.commands.application.CommandScope;
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashOption;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.TopLevelSlashCommandData;

import net.dv8tion.jda.api.entities.Member;

import network.venox.cobalt.MongoProvider;
import network.venox.cobalt.mongo.Survey;

import org.jetbrains.annotations.NotNull;

import xyz.srnyx.lazylibrary.emoji.LazyEmoji;


@Command
public class SurveyCreate {
    @NotNull private final MongoProvider mongo;
    @NotNull private final SurveyCommon surveyCommon;

    public SurveyCreate(@NotNull MongoProvider mongo, @NotNull SurveyCommon surveyCommon) {
        this.mongo = mongo;
        this.surveyCommon = surveyCommon;
    }

    @TopLevelSlashCommandData(scope = CommandScope.GUILD)
    @JDASlashCommand(
            name = "survey",
            subcommand = "create",
            description = "Create a new survey")
    public void surveyCreate(@NotNull GuildSlashEvent event,
                             @SlashOption(description = "The name/title of the survey") @NotNull String name) {
        final Member member = event.getMember();

        // Check permissions
        if (!member.hasPermission(SurveyCommon.REQUIRED_PERMISSION)) {
            event.reply(LazyEmoji.NO + " You don't have permission to create surveys!").setEphemeral(true).queue();
            return;
        }

        // Create Survey
        final Survey survey = new Survey(event.getGuild().getIdLong(), member.getIdLong(), name);
        mongo.database.getMagicCollection(Survey.class).insertOneReturnId(survey);
        event.reply(surveyCommon.getBuilder(survey)).useComponentsV2().setEphemeral(true).queue();
    }
}

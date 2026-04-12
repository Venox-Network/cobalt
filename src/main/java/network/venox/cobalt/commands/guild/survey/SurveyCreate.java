package network.venox.cobalt.commands.guild.survey;

import io.github.freya022.botcommands.api.commands.annotations.Command;
import io.github.freya022.botcommands.api.commands.annotations.UserPermissions;
import io.github.freya022.botcommands.api.commands.application.CommandScope;
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashOption;

import io.github.freya022.botcommands.api.commands.application.slash.annotations.TopLevelSlashCommandData;
import net.dv8tion.jda.api.Permission;

import network.venox.cobalt.MongoProvider;
import network.venox.cobalt.mongo.Survey;

import org.jetbrains.annotations.NotNull;


@Command
public class SurveyCreate {
    @NotNull private final MongoProvider mongo;
    @NotNull private final SurveyCommon surveyCommon;

    public SurveyCreate(@NotNull MongoProvider mongo, @NotNull SurveyCommon surveyCommon) {
        this.mongo = mongo;
        this.surveyCommon = surveyCommon;
    }

    @TopLevelSlashCommandData(scope = CommandScope.GUILD)
    @UserPermissions({Permission.MANAGE_SERVER})
    @JDASlashCommand(
            name = "survey",
            subcommand = "create",
            description = "Create a new survey")
    public void surveyCreate(@NotNull GuildSlashEvent event,
                             @SlashOption(description = "The name/title of the survey") @NotNull String name) {
        final Survey survey = new Survey(name);
        mongo.database.getMagicCollection(Survey.class).insertOneReturnId(survey);
        event.reply(surveyCommon.getBuilder(survey)).useComponentsV2().setEphemeral(true).queue();
    }
}

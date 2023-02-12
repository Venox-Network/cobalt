package network.venox.cobalt.commands.subcommands.warncmd;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import network.venox.cobalt.command.CoCommand;
import network.venox.cobalt.command.CoSubCommand;
import network.venox.cobalt.commands.subcommands.WarnCmd;
import network.venox.cobalt.data.objects.CoWarning;
import network.venox.cobalt.events.CoCommandAutoCompleteInteractionEvent;
import network.venox.cobalt.events.CoSlashCommandInteractionEvent;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;


public class RemoveCmd extends CoSubCommand {
    public RemoveCmd(@NotNull CoCommand parent) {
        super(parent);
    }

    @Override @NotNull
    public String description() {
        return "Removes a global warning from a user";
    }

    @Override @NotNull
    public List<OptionData> options() {
        return List.of(
                new OptionData(OptionType.STRING, "user", "The user to remove a warning from", false, true),
                new OptionData(OptionType.INTEGER, "id", "The ID of the warning to remove", false, true));
    }

    @Override
    public void onCommand(@NotNull CoSlashCommandInteractionEvent event) {
        final JDA jda = event.getJDA();
        final OptionMapping userOption = event.getOption("user");
        final OptionMapping idOption = event.getOption("id");
        if (userOption == null && idOption == null) {
            event.reply("You must specify a user and/or a warning ID").setEphemeral(true).queue();
            return;
        }
        final String moderator = event.getUser().getAsMention();

        if (idOption != null) {
            final int id = idOption.getAsInt();
            final CoWarning warning = cobalt.data.global.getWarning(id);
            if (warning == null) {
                event.reply("No warning found with ID `" + id + "`").setEphemeral(true).queue();
                return;
            }

            // Remove warning
            cobalt.data.global.warnings.remove(warning);

            // Reply and log
            final User user = warning.getUser(jda);
            if (user == null) return;
            event.reply("Removed warning from **" + user.getAsMention() + "**").setEphemeral(true).queue();
            cobalt.config.sendLog("remove warning", "**ID:** " + warning.id() + "\n**User:** " + user.getAsMention() + "\n**Reason:** " + warning.reason() + "\n**Moderator:** " + moderator);
            return;
        }

        final User user = jda.retrieveUserById(userOption.getAsLong()).complete();
        if (user == null) return;
        final List<CoWarning> warnings = getParent(WarnCmd.class).getWarnings(event, user);
        if (warnings == null) return;

        // Remove warnings
        cobalt.data.global.warnings.removeAll(warnings);

        // Reply and log
        event.reply("Removed **" + warnings.size() + "** warning(s) from **" + user.getAsTag() + "**").setEphemeral(true).queue();
        cobalt.config.sendLog("remove warnings", "**User:** " + user.getAsMention() + "\n**Warnings:** " + warnings.size() + "\n**Moderator:** " + moderator);
    }

    @Override @Nullable
    public List<Command.Choice> onAutoComplete(@NotNull CoCommandAutoCompleteInteractionEvent event) {
        return getParent(WarnCmd.class).listRemoveAutoComplete(event);
    }
}

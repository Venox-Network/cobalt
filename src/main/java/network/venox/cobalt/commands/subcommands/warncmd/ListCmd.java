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


public class ListCmd extends CoSubCommand {
    public ListCmd(@NotNull CoCommand parent) {
        super(parent);
    }

    @Override @NotNull
    public String description() {
        return "List all the global warnings (of a user)";
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
            final List<CoWarning> warnings = cobalt.data.global.warnings;
            if (warnings.isEmpty()) {
                event.reply("No warnings found").setEphemeral(true).queue();
                return;
            }

            // Reply
            final StringBuilder builder = new StringBuilder();
            for (final CoWarning warning : warnings) {
                final User user = warning.getUser(jda);
                final User moderator = warning.getModerator(jda);
                if (user != null && moderator != null) builder
                        .append("**ID:** ").append(warning.id())
                        .append("\n**User:** ").append(user.getAsMention())
                        .append("\n**Reason:** ").append(warning.reason())
                        .append("\n**Moderator:** ").append(moderator.getAsMention())
                        .append("\n\n");
            }
            event.reply(builder.toString()).setEphemeral(true).queue();
            return;
        }

        if (idOption != null) {
            final int id = idOption.getAsInt();
            final CoWarning warning = cobalt.data.global.getWarning(id);
            if (warning == null) {
                event.reply("No warning found with ID `" + id + "`").setEphemeral(true).queue();
                return;
            }

            // Reply
            final User user = warning.getUser(jda);
            final User moderator = warning.getModerator(jda);
            if (user != null && moderator != null) event.reply("**ID:** " + id + "\n**User:** " + user.getAsMention() + "\n**Reason:** " + warning.reason() + "\n**Moderator:** " + moderator.getAsMention()).setEphemeral(true).queue();
            return;
        }

        final User user = jda.retrieveUserById(userOption.getAsLong()).complete();
        if (user == null) return;
        final List<CoWarning> warnings = getParent(WarnCmd.class).getWarnings(event, user);
        if (warnings == null) return;

        // Reply
        final StringBuilder builder = new StringBuilder();
        for (final CoWarning warning : warnings) {
            final User moderator = warning.getModerator(jda);
            if (moderator != null) builder
                    .append("**ID:** ").append(warning.id())
                    .append("\n**User:** ").append(user.getAsMention())
                    .append("\n**Reason:** ").append(warning.reason())
                    .append("\n**Moderator:** ").append(moderator.getAsMention())
                    .append("\n\n");
        }
        event.reply(builder.toString()).setEphemeral(true).queue();
    }

    @Override @Nullable
    public List<Command.Choice> onAutoComplete(@NotNull CoCommandAutoCompleteInteractionEvent event) {
        return getParent(WarnCmd.class).listRemoveAutoComplete(event);
    }
}

package network.venox.cobalt.commands.subcommands;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import network.venox.cobalt.Cobalt;
import network.venox.cobalt.command.CoParentCommand;
import network.venox.cobalt.command.CoSubCommand;
import network.venox.cobalt.commands.subcommands.warncmd.AddCmd;
import network.venox.cobalt.commands.subcommands.warncmd.ListCmd;
import network.venox.cobalt.commands.subcommands.warncmd.RemoveCmd;
import network.venox.cobalt.data.objects.CoWarning;
import network.venox.cobalt.events.CoCommandAutoCompleteInteractionEvent;
import network.venox.cobalt.events.CoSlashCommandInteractionEvent;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;


public class WarnCmd extends CoParentCommand {
    public WarnCmd(@NotNull Cobalt cobalt) {
        super(cobalt);
    }

    @Override @NotNull
    public String description() {
        return "Commands to manage global warnings for users";
    }

    @Override @NotNull
    public List<CoSubCommand> subCommands() {
        return List.of(
                new AddCmd(this),
                new ListCmd(this),
                new RemoveCmd(this));
    }

    @Nullable
    public List<CoWarning> getWarnings(@NotNull CoSlashCommandInteractionEvent event, @NotNull User user) {
        final List<CoWarning> warnings = cobalt.data.global.getWarnings(user.getIdLong());
        if (warnings.isEmpty()) {
            event.reply("No warnings found").setEphemeral(true).queue();
            return null;
        }
        return warnings;
    }

    @Nullable
    public List<Command.Choice> listRemoveAutoComplete(@NotNull CoCommandAutoCompleteInteractionEvent event) {
        final String option = event.getFocusedOption().getName();

        // user
        if (option.equals("user")) {
            final JDA jda = event.getJDA();
            return cobalt.data.global.warnings.stream()
                    .map(warning -> {
                        final User user = warning.getUser(jda);
                        if (user == null) return null;
                        return new Command.Choice(user.getAsTag(), user.getIdLong());
                    })
                    .filter(Objects::nonNull)
                    .distinct()
                    .toList();
        }

        // id
        if (option.equals("id")) {
            List<CoWarning> warnings = cobalt.data.global.warnings;
            final OptionMapping userOption = event.getOption("user");
            if (userOption != null) warnings = cobalt.data.global.getWarnings(userOption.getAsLong());
            return warnings.stream()
                    .map(warning -> new Command.Choice(String.valueOf(warning.id()), warning.id()))
                    .toList();
        }

        return null;
    }
}

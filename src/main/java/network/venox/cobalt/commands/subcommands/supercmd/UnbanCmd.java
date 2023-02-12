package network.venox.cobalt.commands.subcommands.supercmd;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import network.venox.cobalt.data.objects.CoEmbed;
import network.venox.cobalt.command.CoCommand;
import network.venox.cobalt.command.CoSubCommand;
import network.venox.cobalt.data.objects.CoSuperBan;
import network.venox.cobalt.events.CoCommandAutoCompleteInteractionEvent;
import network.venox.cobalt.events.CoSlashCommandInteractionEvent;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;


public class UnbanCmd extends CoSubCommand {
    public UnbanCmd(@NotNull CoCommand parent) {
        super(parent);
    }

    @Override @NotNull
    public String description() {
        return "Unbans the specified user from all Venox servers";
    }

    @Override @NotNull
    public List<OptionData> options() {
        return Collections.singletonList(new OptionData(OptionType.STRING, "user", "The ID of the user to unban", true));
    }

    @Override
    public void onCommand(@NotNull CoSlashCommandInteractionEvent event) {
        final OptionMapping userOption = event.getOption("user");
        if (userOption == null) return;
        final JDA jda = event.getJDA();

        // Get user
        final long userId = userOption.getAsLong();
        final User user = jda.retrieveUserById(userId).complete();
        if (user == null) {
            event.replyEmbeds(CoEmbed.invalidArgument(userId).build()).setEphemeral(true).queue();
            return;
        }

        // Get ban
        final CoSuperBan ban = cobalt.data.global.getSuperBan(userId);
        if (ban == null) {
            event.reply(user.getAsMention() + " is not super-banned!").setEphemeral(true).queue();
            return;
        }

        // Unban user
        cobalt.data.global.superBans.remove(ban);
        ban.unban(jda);

        // Reply
        event.reply(user.getAsMention() + " has been unbanned from all Venox servers").setEphemeral(true).queue();
    }

    @Override @NotNull
    public Set<Command.Choice> onAutoComplete(@NotNull CoCommandAutoCompleteInteractionEvent event) {
        final JDA jda = event.getJDA();
        return cobalt.data.global.superBans.stream()
                .map(ban -> {
                    final User user = ban.getUser(jda);
                    if (user == null) return null;
                    return new Command.Choice(user.getAsTag(), user.getIdLong());
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }
}

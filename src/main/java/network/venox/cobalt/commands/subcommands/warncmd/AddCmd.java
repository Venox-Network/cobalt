package network.venox.cobalt.commands.subcommands.warncmd;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import network.venox.cobalt.command.CoCommand;
import network.venox.cobalt.command.CoSubCommand;
import network.venox.cobalt.data.objects.CoWarning;
import network.venox.cobalt.events.CoCommandAutoCompleteInteractionEvent;
import network.venox.cobalt.events.CoSlashCommandInteractionEvent;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;


public class AddCmd extends CoSubCommand {
    public AddCmd(@NotNull CoCommand parent) {
        super(parent);
    }

    @Override @NotNull
    public String description() {
        return "Adds a global warning to a user";
    }

    @Override @NotNull
    public List<OptionData> options() {
        return List.of(
                new OptionData(OptionType.STRING, "user", "The user to add a warning to", true, true),
                new OptionData(OptionType.STRING, "reason", "The reason for the warning", true));
    }

    @Override
    public void onCommand(@NotNull CoSlashCommandInteractionEvent event) {
        final OptionMapping userOption = event.getOption("user");
        final OptionMapping reasonOption = event.getOption("reason");
        if (userOption == null || reasonOption == null) return;
        final long userId = userOption.getAsLong();
        final User user = event.getJDA().retrieveUserById(userId).complete();
        if (user == null) return;
        final User moderator = event.getUser();
        final String reason = reasonOption.getAsString();

        // Add warning
        final CoWarning warning = new CoWarning(cobalt.data.global.getNextWarningId(), userId, reason, moderator.getIdLong());
        cobalt.data.global.warnings.add(warning);
        final int count = cobalt.data.global.getWarnings(userId).size();

        // Reply
        event.reply("**Warned " + user.getAsMention() + " for:**\n> " + reason + "\nThey now have **" + count + "** global warning(s)!").setEphemeral(true).queue();

        // DM user
        user.openPrivateChannel()
                .flatMap(channel -> channel.sendMessage("**You have been globally warned in Venox Network for:**\n> " + reason + "\nYou now have **" + count + "** global warning(s)!"))
                .queue();

        // Log
        cobalt.config.sendLog("add warning", "**User:**" + user.getAsMention() + "\n**Reason:**" + reason + "\n**Moderator:**" + moderator.getAsMention() + "\n**Warnings:**" + count);
    }

    @Override @Nullable
    public List<Command.Choice> onAutoComplete(@NotNull CoCommandAutoCompleteInteractionEvent event) {
        if (!event.getFocusedOption().getName().equals("user")) return null;
        final Guild guild = event.getGuild();
        if (guild == null) return null;
        return guild.getMembers().stream()
                .filter(member -> !member.getUser().isBot())
                .map(member -> new Command.Choice(member.getUser().getAsTag(), member.getIdLong()))
                .sorted(Comparator.comparing(choice -> choice.getName().toLowerCase()))
                .toList();
    }
}

package network.venox.cobalt.commands.subcommands.supercmd;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import network.venox.cobalt.command.CoCommand;
import network.venox.cobalt.command.CoSubCommand;
import network.venox.cobalt.events.CoSlashCommandInteractionEvent;
import network.venox.cobalt.utility.CoMapper;

import org.jetbrains.annotations.NotNull;

import java.util.List;


public class KickCmd extends CoSubCommand {
    public KickCmd(@NotNull CoCommand parent) {
        super(parent);
    }

    @Override @NotNull
    public String description() {
        return "Kicks a user from every Venox Network server";
    }

    @Override @NotNull
    public List<OptionData> options() {
        return List.of(
                new OptionData(OptionType.STRING, "user", "The ID of the user to kick", true),
                new OptionData(OptionType.STRING, "reason", "The reason for the kick", true));
    }

    @Override
    public void onCommand(@NotNull CoSlashCommandInteractionEvent event) {
        final OptionMapping userOption = event.getOption("user");
        final OptionMapping reasonOption = event.getOption("reason");
        if (userOption == null || reasonOption == null) return;
        final User user = CoMapper.handleException(() -> event.getJDA().retrieveUserById(userOption.getAsLong()).complete());
        if (user == null) return;
        final String reason = reasonOption.getAsString();

        // Send message to user
        user.openPrivateChannel()
                .flatMap(channel -> channel.sendMessage("You have been kicked from all Venox Network servers for the following reason:\n> " + reason))
                .complete();

        // Kick user from all guilds
        for (final Guild guild : event.getJDA().getGuilds()) guild.kick(user).reason(reason).queue();

        // Reply
        event.reply("Kicked " + user.getAsMention() + " from all Venox Network servers").setEphemeral(true).queue();

        // Log
        cobalt.config.sendLog("superkick", "**User:** " + user.getAsMention() + "\n**Reason:** " + reason + "\n**Moderator:** " + event.getUser().getAsMention());
    }
}

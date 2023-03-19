package network.venox.cobalt.commands.guild;

import com.freya02.botcommands.api.annotations.CommandMarker;
import com.freya02.botcommands.api.annotations.Dependency;
import com.freya02.botcommands.api.annotations.UserPermissions;
import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.CommandPath;
import com.freya02.botcommands.api.application.CommandScope;
import com.freya02.botcommands.api.application.annotations.AppOption;
import com.freya02.botcommands.api.application.slash.GuildSlashEvent;
import com.freya02.botcommands.api.application.slash.annotations.JDASlashCommand;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.managers.channel.concrete.VoiceChannelManager;

import network.venox.cobalt.Cobalt;
import network.venox.cobalt.data.objects.CoStatsChannel;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Set;


@CommandMarker @UserPermissions({Permission.MANAGE_CHANNEL, Permission.MANAGE_SERVER})
public class StatsCmd extends ApplicationCommand {
    @Dependency private Cobalt cobalt;

    @JDASlashCommand(
            scope = CommandScope.GUILD,
            name = "stats",
            subcommand = "list",
            description = "List all stat channels for this guild")
    public void listCommand(@NotNull GuildSlashEvent event) {
        // Get statsChannels
        final Set<CoStatsChannel> statsChannels = cobalt.data.getGuild(event.getGuild()).statsChannels;
        if (statsChannels.isEmpty()) {
            event.reply("No stat channels found").setEphemeral(true).queue();
            return;
        }

        // Reply
        final StringBuilder builder = new StringBuilder();
        for (final CoStatsChannel statsChannel : statsChannels) builder.append("<#").append(statsChannel.id).append("> (`").append(statsChannel.type.name()).append("`), ");
        builder.setLength(builder.length() - 2);
        event.reply(builder.toString()).setEphemeral(true).queue();
    }

    @JDASlashCommand(
            scope = CommandScope.GUILD,
            name = "stats",
            subcommand = "add",
            description = "Add a stat channel for this guild")
    public void addCommand(@NotNull GuildSlashEvent event,
                           @AppOption(description = "The text to be displayed in the stat channel. Use '%count%' for the value of the stat") @NotNull String text,
                           @AppOption(description = "The stat to be displayed for %count% in the text") @NotNull String stat) {
        // Check if text contains %count%
        if (!text.contains("%count%")) {
            event.reply("The text must contain `%count%`").setEphemeral(true).queue();
            return;
        }

        // Get stat type
        final CoStatsChannel.CoStatsType statsType = CoStatsChannel.CoStatsType.getType(stat);
        if (statsType == null) {
            event.reply("Invalid stat type: `" + stat + "`").setEphemeral(true).queue();
            return;
        }

        // Create VoiceChannel
        final Guild guild = event.getGuild();
        guild.createVoiceChannel(text)
                .addRolePermissionOverride(guild.getPublicRole().getIdLong(), Permission.VIEW_CHANNEL.getRawValue(), Permission.VOICE_CONNECT.getRawValue())
                .flatMap(channel -> {
                    // Get statsChannel
                    final CoStatsChannel statsChannel = new CoStatsChannel(cobalt, channel.getIdLong(), text, statsType);
                    cobalt.data.getGuild(guild).statsChannels.add(statsChannel);

                    // Attempt to update channel
                    final VoiceChannelManager manager = statsChannel.update(guild);
                    if (manager == null) return event.reply("Failed to add stat channel").setEphemeral(true);

                    // Reply
                    return manager
                            .flatMap(v -> event.reply("Added stat channel with text `" + text + "` and stat `" + stat + "`: " + channel.getAsMention()).setEphemeral(true));
                })
                .queue();
    }

    @Override @NotNull
    public List<Command.Choice> getOptionChoices(@Nullable Guild guild, @NotNull CommandPath commandPath, int optionIndex) {
        if (optionIndex == 1) return List.of(
                new Command.Choice("Member count", "MEMBERS"),
                new Command.Choice("Human count", "HUMANS"));
        return Collections.emptyList();
    }
}

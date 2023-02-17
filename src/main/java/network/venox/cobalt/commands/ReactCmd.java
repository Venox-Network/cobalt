package network.venox.cobalt.commands;

import com.freya02.botcommands.api.annotations.CommandMarker;
import com.freya02.botcommands.api.annotations.Dependency;
import com.freya02.botcommands.api.annotations.UserPermissions;
import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.CommandScope;
import com.freya02.botcommands.api.application.annotations.AppOption;
import com.freya02.botcommands.api.application.slash.GuildSlashEvent;
import com.freya02.botcommands.api.application.slash.annotations.JDASlashCommand;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import network.venox.cobalt.Cobalt;
import network.venox.cobalt.data.CoGuild;
import network.venox.cobalt.data.objects.CoReactChannel;
import network.venox.cobalt.utility.CoMapper;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;


@CommandMarker @UserPermissions({Permission.MANAGE_CHANNEL, Permission.MESSAGE_MANAGE, Permission.MESSAGE_ADD_REACTION})
public class ReactCmd extends ApplicationCommand {
    @Dependency private Cobalt cobalt;

    @JDASlashCommand(
            scope = CommandScope.GUILD,
            name = "react",
            subcommand = "set",
            description = "Sets the emoji(s) for a channel")
    public void setCommand(@NotNull GuildSlashEvent event,
                          @AppOption(description = "The channel to manage") @Nullable TextChannel channel,
                          @AppOption(description = "The emojis to set. If empty, channel will be dynamic") @Nullable String emojis) {
        final TextChannel channelSet = channel != null ? channel : CoMapper.handleException(() -> event.getChannel().asTextChannel());
        if (channelSet == null) return;
        final CoGuild guild = cobalt.data.getGuild(event.getGuild());

        // Dynamic
        if (emojis == null) {
            final CoReactChannel reactChannel = guild.getReactChannel(channelSet.getIdLong());
            if (reactChannel != null) {
                reactChannel.emojis = null;
            } else {
                guild.reactChannels.add(new CoReactChannel(channelSet.getIdLong(), null));
            }
            event.reply(channelSet.getAsMention() + " has been set as a dynamic react channel").setEphemeral(true).queue();
            return;
        }

        // Static
        final List<String> emojiList = Arrays.asList(emojis.split(" "));
        if (emojiList.size() > 20) emojiList.subList(20, emojiList.size()).clear();
        final CoReactChannel reactChannel = guild.getReactChannel(channelSet.getIdLong());
        if (reactChannel != null) {
            reactChannel.emojis = emojiList;
        } else {
            guild.reactChannels.add(new CoReactChannel(channelSet.getIdLong(), emojiList));
        }
        event.reply(channelSet.getAsMention() + " has been set as a static react channel with emojis: " + String.join(" ", emojiList)).setEphemeral(true).queue();
    }

    @JDASlashCommand(
            scope = CommandScope.GUILD,
            name = "react",
            subcommand = "unset",
            description = "Unset a channel as a reaction channel")
    public void unsetCommand(@NotNull GuildSlashEvent event,
                          @AppOption(description = "The channel to unset as a reaction channel") @Nullable TextChannel channel) {
        if (!cobalt.config.checkIsOwner(event)) return;
        final TextChannel channelSet = channel != null ? channel : CoMapper.handleException(() -> event.getChannel().asTextChannel());
        if (channelSet == null) return;
        final CoGuild guild = cobalt.data.getGuild(event.getGuild());

        // Get reaction channel
        final CoReactChannel reactChannel = guild.getReactChannel(channelSet.getIdLong());
        if (reactChannel == null) {
            event.reply("This channel is not a reaction channel").setEphemeral(true).queue();
            return;
        }

        // Remove reaction channel
        guild.reactChannels.remove(reactChannel);
        event.reply(channelSet.getAsMention() + " is no longer a reaction channel").setEphemeral(true).queue();
    }
}

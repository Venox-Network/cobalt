package network.venox.cobalt.commands.guild.react;

import com.mongodb.client.model.Filters;

import io.github.freya022.botcommands.api.commands.annotations.Command;
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashOption;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import network.venox.cobalt.MongoProvider;
import network.venox.cobalt.mongo.ReactChannel;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.javautilities.MiscUtility;

import xyz.srnyx.lazylibrary.LazyEmbed;
import xyz.srnyx.lazylibrary.emoji.LazyEmoji;


@Command
public class ReactUnset {
    @NotNull private final MongoProvider mongo;

    public ReactUnset(@NotNull MongoProvider mongo) {
        this.mongo = mongo;
    }

    @JDASlashCommand(
            name = "react",
            subcommand = "unset",
            description = "Unset a channel as a reaction channel")
    public void unsetCommand(@NotNull GuildSlashEvent event,
                             @SlashOption(description = "The channel to unset as a reaction channel") @Nullable TextChannel channel) {
        // Get channel
        if (channel == null) {
            channel = MiscUtility.handleException(() -> event.getChannel().asTextChannel()).orElse(null);
            if (channel == null) {
                event.reply(LazyEmoji.NO + " Please specify a channel to unset!").setEphemeral(true).queue();
                return;
            }
        }

        // Check permissions
        if (!event.getMember().hasPermission(channel, Permission.MANAGE_CHANNEL, Permission.MESSAGE_MANAGE, Permission.MESSAGE_ADD_REACTION)) {
            event.replyEmbeds(LazyEmbed.noPermission().build()).setEphemeral(true).queue();
            return;
        }

        // Delete reaction channel
        final ReactChannel reactChannel = mongo.database.getMagicCollection(ReactChannel.class).findOneAndDelete(Filters.eq("_id", channel.getIdLong()));
        if (reactChannel == null) {
            event.reply(LazyEmoji.NO + " This channel is not a reaction channel!").setEphemeral(true).queue();
            return;
        }

        // Reply
        event.reply(LazyEmoji.YES + " " + channel.getAsMention() + " is no longer a reaction channel").setEphemeral(true).queue();
    }
}

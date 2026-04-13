package network.venox.cobalt.commands.guild.limitedmessages;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;

import io.github.freya022.botcommands.api.commands.annotations.Command;
import io.github.freya022.botcommands.api.commands.annotations.UserPermissions;
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.ChannelTypes;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashOption;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;

import network.venox.cobalt.MongoProvider;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.lazylibrary.LazyEmbed;
import xyz.srnyx.lazylibrary.emoji.LazyEmoji;


@Command
public class LimitedMessagesSet {
    @NotNull private final MongoProvider mongo;

    public LimitedMessagesSet(@NotNull MongoProvider mongo) {
        this.mongo = mongo;
    }

    @UserPermissions({Permission.MANAGE_CHANNEL, Permission.MESSAGE_MANAGE})
    @JDASlashCommand(
            name = "limitedmessages",
            subcommand = "set",
            description = "Only allow a certain amount of messages in a channel")
    public void limitedMessagesSet(@NotNull GuildSlashEvent event,
                                   @SlashOption(description = "The amount of messages to allow") int limit,
                                   @SlashOption(description = "The channel to enable limited messages in") @ChannelTypes({ChannelType.TEXT, ChannelType.NEWS}) @Nullable GuildChannel channel) {
        if (channel == null) channel = event.getChannel().asGuildMessageChannel();

        // Check permissions
        if (!event.getMember().hasPermission(channel, Permission.MANAGE_CHANNEL, Permission.MESSAGE_MANAGE)) {
            event.replyEmbeds(LazyEmbed.noPermission().build()).setEphemeral(true).queue();
            return;
        }

        // Edit existing limitedMessages and reply
        final network.venox.cobalt.mongo.LimitedMessages existing = mongo.database.getMagicCollection(network.venox.cobalt.mongo.LimitedMessages.class).findOneAndUpdate(
                Filters.and(
                        Filters.eq("_id", channel.getIdLong()),
                        Filters.eq(network.venox.cobalt.mongo.LimitedMessages.PROP_GUILD, event.getGuild().getIdLong())),
                Updates.set(network.venox.cobalt.mongo.LimitedMessages.PROP_LIMIT, limit));
        if (existing != null) {
            existing.checkAllUsers(event.getJDA());
            event.reply(LazyEmoji.YES + " Updated per-user message limit in " + channel.getAsMention() + " to `" + limit + "`").setEphemeral(true).queue();
            return;
        }

        // Reply (new)
        event.reply(LazyEmoji.YES + " Set per-user message limit in " + channel.getAsMention() + " to `" + limit + "`").setEphemeral(true).queue();
    }
}

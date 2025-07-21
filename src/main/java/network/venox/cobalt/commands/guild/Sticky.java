package network.venox.cobalt.commands.guild;

import com.freya02.botcommands.api.annotations.CommandMarker;
import com.freya02.botcommands.api.annotations.Dependency;
import com.freya02.botcommands.api.annotations.UserPermissions;
import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.CommandScope;
import com.freya02.botcommands.api.application.annotations.AppOption;
import com.freya02.botcommands.api.application.slash.GuildSlashEvent;
import com.freya02.botcommands.api.application.slash.annotations.JDASlashCommand;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;

import network.venox.cobalt.Cobalt;
import network.venox.cobalt.mongo.StickyMessage;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.javautilities.manipulation.Mapper;

import xyz.srnyx.lazylibrary.LazyEmbed;
import xyz.srnyx.lazylibrary.LazyEmoji;

import xyz.srnyx.magicmongo.MagicCollection;

import java.util.Optional;


@CommandMarker @UserPermissions({Permission.MANAGE_CHANNEL, Permission.MESSAGE_MANAGE, Permission.MESSAGE_SEND})
public class Sticky extends ApplicationCommand {
    @Dependency private Cobalt bot;

    @JDASlashCommand(
            scope = CommandScope.GUILD,
            name = "sticky",
            description = "Sticky a message to keep it as the last message in the current channel")
    public void stickyCommand(@NotNull GuildSlashEvent event,
                              @AppOption(description = "The message to sticky. If empty, sticky will be removed") @Nullable String message) {
        final TextChannel channel = event.getChannel().asTextChannel();
        final MagicCollection<StickyMessage> collection = bot.mongo.getMagicCollection(StickyMessage.class);

        // Delete existing sticky message
        if (message == null) {
            final StickyMessage stickyMessage = collection.findOneAndDelete(Filters.eq("_id", channel.getIdLong()));
            if (stickyMessage != null) stickyMessage.delete(channel);
            event.reply(LazyEmoji.YES + " Sticky message has been removed from " + channel.getAsMention()).setEphemeral(true).queue();
            return;
        }

        // Get messageId
        final Optional<Long> messageId = Mapper.toLong(message);
        if (messageId.isEmpty()) {
            event.replyEmbeds(LazyEmbed.invalidArgument("message", message).build(bot)).setEphemeral(true).queue();
            return;
        }

        // Upsert new sticky message
        channel.retrieveMessageById(messageId.get())
                .flatMap(sentMessage -> {
                    final MessageChannelUnion messageChannel = sentMessage.getChannel();
                    collection
                            .findOneAndUpsert(
                                    Filters.and(
                                            Filters.eq("_id", messageChannel.getIdLong()),
                                            Filters.eq(StickyMessage.PROP_GUILD, sentMessage.getGuildIdLong())),
                                    Updates.combine(
                                            Updates.set(StickyMessage.PROP_MESSAGE, new StickyMessage.MongoMessage(sentMessage)),
                                            Updates.set(StickyMessage.PROP_CURRENT, sentMessage.getIdLong())))
                            .send(bot, messageChannel);
                    return event.reply(LazyEmoji.YES + " " + sentMessage.getJumpUrl() + " has been set as " + messageChannel.getAsMention() + "'s sticky message").setEphemeral(true);
                })
                .queue();
    }
}

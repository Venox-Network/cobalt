package network.venox.cobalt.apps.message;

import com.freya02.botcommands.api.annotations.CommandMarker;
import com.freya02.botcommands.api.annotations.Dependency;
import com.freya02.botcommands.api.annotations.UserPermissions;
import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.CommandScope;
import com.freya02.botcommands.api.application.context.annotations.JDAMessageCommand;
import com.freya02.botcommands.api.application.context.message.GuildMessageEvent;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;

import network.venox.cobalt.Cobalt;

import org.jetbrains.annotations.NotNull;


@CommandMarker @UserPermissions({Permission.MANAGE_CHANNEL, Permission.MESSAGE_MANAGE, Permission.MESSAGE_SEND})
public class Sticky extends ApplicationCommand {
    @Dependency private Cobalt bot;

    @JDAMessageCommand(
            scope = CommandScope.GUILD,
            name = "Sticky message")
    public void stickyContext(@NotNull GuildMessageEvent event) {
        final MessageChannelUnion channelUnion = event.getChannel();
        if (channelUnion == null) return;
        final TextChannel channel = channelUnion.asTextChannel();
        final Message message = event.getTarget();

        // Upsert and send/edit sticky message
        bot.mongo.getMagicCollection(network.venox.cobalt.mongo.StickyMessage.class)
                .findOneAndUpsert(
                        Filters.and(
                                Filters.eq("_id", channel.getIdLong()),
                                Filters.eq(network.venox.cobalt.mongo.StickyMessage.PROP_GUILD, event.getGuild().getIdLong())),
                        Updates.combine(
                                Updates.set(network.venox.cobalt.mongo.StickyMessage.PROP_MESSAGE, new network.venox.cobalt.mongo.StickyMessage.MongoMessage(message)),
                                Updates.set(network.venox.cobalt.mongo.StickyMessage.PROP_CURRENT, message.getIdLong())))
                .send(bot, channel);

        // Reply
        event.reply(message.getJumpUrl() + " has been set as " + channel.getAsMention() + "'s sticky message").setEphemeral(true).queue();
    }
}

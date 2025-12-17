package network.venox.cobalt.apps.message;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;

import io.github.freya022.botcommands.api.commands.annotations.Command;
import io.github.freya022.botcommands.api.commands.annotations.UserPermissions;
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand;
import io.github.freya022.botcommands.api.commands.application.CommandScope;
import io.github.freya022.botcommands.api.commands.application.context.annotations.JDAMessageCommand;
import io.github.freya022.botcommands.api.commands.application.context.message.GuildMessageEvent;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;

import network.venox.cobalt.MongoProvider;
import network.venox.cobalt.mongo.MongoMessage;

import org.jetbrains.annotations.NotNull;


@Command
public class StickyApp extends ApplicationCommand {
    @NotNull private final MongoProvider mongo;

    public StickyApp(@NotNull MongoProvider mongo) {
        this.mongo = mongo;
    }

    @JDAMessageCommand(
            scope = CommandScope.GUILD,
            name = "Sticky message")
    @UserPermissions({Permission.MANAGE_CHANNEL, Permission.MESSAGE_MANAGE, Permission.MESSAGE_SEND})
    public void stickyContext(@NotNull GuildMessageEvent event) {
        final MessageChannelUnion channelUnion = event.getChannel();
        if (channelUnion == null) return;
        final TextChannel channel = channelUnion.asTextChannel();
        final Message message = event.getTarget();

        // Upsert and send/edit sticky message
        mongo.database.getMagicCollection(network.venox.cobalt.mongo.StickyMessage.class)
                .findOneAndUpsert(
                        Filters.and(
                                Filters.eq("_id", channel.getIdLong()),
                                Filters.eq(network.venox.cobalt.mongo.StickyMessage.PROP_GUILD, event.getGuild().getIdLong())),
                        Updates.combine(
                                Updates.set(network.venox.cobalt.mongo.StickyMessage.PROP_MESSAGE, new MongoMessage(message)),
                                Updates.set(network.venox.cobalt.mongo.StickyMessage.PROP_CURRENT, message.getIdLong())))
                .send(mongo, channel);

        // Reply
        event.reply(message.getJumpUrl() + " has been set as " + channel.getAsMention() + "'s sticky message").setEphemeral(true).queue();
    }
}

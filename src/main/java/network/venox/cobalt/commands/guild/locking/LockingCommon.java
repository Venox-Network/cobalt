package network.venox.cobalt.commands.guild.locking;

import io.github.freya022.botcommands.api.commands.application.ApplicationCommand;
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent;
import io.github.freya022.botcommands.api.commands.application.slash.autocomplete.annotations.AutocompleteHandler;
import io.github.freya022.botcommands.api.core.annotations.Handler;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;

import network.venox.cobalt.MongoProvider;
import network.venox.cobalt.mongo.Server;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.lazylibrary.LazyEmoji;

import java.util.*;


@Handler
public class LockingCommon extends ApplicationCommand {
    @NotNull public static final String AC_PRESET = "LockingLock.ac.preset";
    @NotNull public static final Set<Permission> PERMISSIONS = Set.of(Permission.MESSAGE_SEND, Permission.MESSAGE_ADD_REACTION, Permission.CREATE_PUBLIC_THREADS, Permission.CREATE_PRIVATE_THREADS, Permission.MESSAGE_SEND_IN_THREADS);

    @NotNull private final MongoProvider mongo;

    public LockingCommon(@NotNull MongoProvider mongo) {
        this.mongo = mongo;
    }

    @AutocompleteHandler(AC_PRESET) @NotNull
    public List<String> onAutoCompleteServer(@NotNull CommandAutoCompleteInteractionEvent event) {
        return mongo.database.getMagicCollection(Server.class).findOne("_id", Objects.requireNonNull(event.getGuild()).getIdLong())
                .map(server -> server.lockPresets)
                .map(lockPresets -> (List<String>) new ArrayList<>(lockPresets.keySet()))
                .orElse(Collections.emptyList());
    }

    @Nullable
    public static TextChannel initialize(@NotNull GuildSlashEvent event, @Nullable TextChannel channel) {
        // Get channel
        if (channel == null) {
            final MessageChannelUnion channelUnion = event.getChannel();
            if (channelUnion.getType() != ChannelType.TEXT) {
                event.reply(LazyEmoji.NO + " You can only lock text channels!").setEphemeral(true).queue();
                return null;
            }
            channel = channelUnion.asTextChannel();
        }

        // Check permissions
        if (!event.getMember().hasPermission(channel, Permission.MANAGE_CHANNEL, Permission.MANAGE_PERMISSIONS)) {
            event.reply(LazyEmoji.NO + " You don't have permission to lock this channel!").setEphemeral(true).queue();
            return null;
        }

        return channel;
    }

    @NotNull
    public static String getMessage(@NotNull Set<Long> roles, @Nullable String content) {
        final StringBuilder builder = new StringBuilder("### " + LazyEmoji.LOCK_CLEAR + " Channel locked!\nOnly these roles can send messages here: " + getRolesString(roles));
        if (content != null) builder.append("\n\n>>> ").append(content);
        return builder.toString();
    }

    @NotNull
    public static String getRolesString(@NotNull Set<Long> roles) {
        if (roles.isEmpty()) return "None!";
        final StringBuilder builder = new StringBuilder();
        for (final long role : roles) builder.append("<@&").append(role).append("> ");
        return builder.substring(0, builder.length() - 1);
    }
}

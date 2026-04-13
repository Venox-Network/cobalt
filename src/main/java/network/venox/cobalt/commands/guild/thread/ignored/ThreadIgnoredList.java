package network.venox.cobalt.commands.guild.thread.ignored;

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
import network.venox.cobalt.mongo.AutoThread;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.lazylibrary.LazyEmbed;
import xyz.srnyx.lazylibrary.emoji.LazyEmoji;

import java.util.Set;


@Command
public class ThreadIgnoredList  {
    @NotNull private final MongoProvider mongo;

    public ThreadIgnoredList(@NotNull MongoProvider mongo) {
        this.mongo = mongo;
    }

    @UserPermissions({Permission.MANAGE_CHANNEL, Permission.MANAGE_THREADS})
    @JDASlashCommand(
            name = "thread",
            group = "ignored",
            subcommand = "list",
            description = "List the ignored phrases/roles for an auto-thread channel")
    public void threadIgnoredList(@NotNull GuildSlashEvent event,
                                  @SlashOption(description = "The channel to list the ignored phrases/roles for") @ChannelTypes({ChannelType.TEXT, ChannelType.NEWS}) @Nullable GuildChannel channel) {
        if (channel == null) channel = event.getGuildChannel();

        // Check permissions
        if (!event.getMember().hasPermission(channel, Permission.MANAGE_CHANNEL, Permission.MANAGE_THREADS)) {
            event.replyEmbeds(LazyEmbed.noPermission().build()).setEphemeral(true).queue();
            return;
        }

        // Get AutoThread
        final AutoThread threadChannel = mongo.database.getMagicCollection(AutoThread.class)
                .findOne("_id", channel.getIdLong())
                .orElse(null);
        if (threadChannel == null) {
            event.reply(LazyEmoji.NO + " Auto-threading for " + channel.getAsMention() + " is not enabled").setEphemeral(true).queue();
            return;
        }

        // Get ignored phrases/roles
        final Set<String> ignoredPhrases = threadChannel.ignoredPhrases();
        final Set<Long> ignoredRoles = threadChannel.ignoredRoles();
        final boolean hasPhrases = !ignoredPhrases.isEmpty();
        final boolean hasRoles = !ignoredRoles.isEmpty();
        if (!hasPhrases && !hasRoles) {
            event.reply(LazyEmoji.NO + " There are no ignored phrases/roles for " + channel.getAsMention() + "'s auto-threading").setEphemeral(true).queue();
            return;
        }

        final StringBuilder builder = new StringBuilder();
        if (hasPhrases) {
            builder.append("**Ignored Phrases:** ");
            for (final String phrase : ignoredPhrases) builder.append("`").append(phrase).append("` ");
        }
        if (hasRoles) {
            if (hasPhrases) builder.append("\n");
            builder.append("**Ignored Roles:** ");
            for (long roleId : ignoredRoles) builder.append("<@&").append(roleId).append(">").append(" ");
        }

        event.reply(builder.toString()).setEphemeral(true).queue();
    }
}

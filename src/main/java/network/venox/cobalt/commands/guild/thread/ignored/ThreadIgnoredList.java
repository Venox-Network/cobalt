package network.venox.cobalt.commands.guild.thread.ignored;

import com.freya02.botcommands.api.annotations.CommandMarker;
import com.freya02.botcommands.api.annotations.Dependency;
import com.freya02.botcommands.api.annotations.UserPermissions;
import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.annotations.AppOption;
import com.freya02.botcommands.api.application.slash.GuildSlashEvent;
import com.freya02.botcommands.api.application.slash.annotations.ChannelTypes;
import com.freya02.botcommands.api.application.slash.annotations.JDASlashCommand;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;

import network.venox.cobalt.Cobalt;
import network.venox.cobalt.mongo.AutoThread;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.lazylibrary.LazyEmbed;
import xyz.srnyx.lazylibrary.LazyEmoji;

import java.util.Set;


@CommandMarker @UserPermissions({Permission.MANAGE_CHANNEL, Permission.MANAGE_THREADS})
public class ThreadIgnoredList extends ApplicationCommand {
    @Dependency private Cobalt bot;

    @JDASlashCommand(
            name = "thread",
            group = "ignored",
            subcommand = "list",
            description = "List the ignored phrases/roles for an auto-thread channel")
    public void ignoredListCommand(@NotNull GuildSlashEvent event,
                                   @AppOption(description = "The channel to list the ignored phrases/roles for") @ChannelTypes({ChannelType.TEXT, ChannelType.NEWS}) @Nullable GuildChannel channel) {
        if (channel == null) channel = event.getGuildChannel();

        // Check permissions
        if (!event.getMember().hasPermission(channel, Permission.MANAGE_CHANNEL, Permission.MANAGE_THREADS)) {
            event.replyEmbeds(LazyEmbed.noPermission().build(bot)).setEphemeral(true).queue();
            return;
        }

        // Get AutoThread
        final AutoThread threadChannel = bot.mongo.getMagicCollection(AutoThread.class)
                .findOne("_id", channel.getIdLong())
                .orElse(null);
        if (threadChannel == null) {
            event.reply(LazyEmoji.NO + " Auto-threading for " + channel.getAsMention() + " is not enabled").setEphemeral(true).queue();
            return;
        }

        // Get ignored phrases/roles
        final Set<String> ignoredPhrases = threadChannel.ignoredPhrases;
        final boolean hasPhrases = !ignoredPhrases.isEmpty();
        final boolean hasRoles = !threadChannel.ignoredRoles.isEmpty();
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
            for (long roleId : threadChannel.ignoredRoles) builder.append("<@&").append(roleId).append(">").append(" ");
        }

        event.reply(builder.toString()).setEphemeral(true).queue();
    }
}

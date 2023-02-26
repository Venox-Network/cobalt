package network.venox.cobalt.commands.guild;

import com.freya02.botcommands.api.annotations.CommandMarker;
import com.freya02.botcommands.api.annotations.Dependency;
import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.CommandScope;
import com.freya02.botcommands.api.application.annotations.AppOption;
import com.freya02.botcommands.api.application.slash.GuildSlashEvent;
import com.freya02.botcommands.api.application.slash.annotations.JDASlashCommand;

import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;

import network.venox.cobalt.Cobalt;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;


@CommandMarker
public class AutoDeleteCmd extends ApplicationCommand {
    @Dependency private Cobalt cobalt;

    @JDASlashCommand(
            scope = CommandScope.GUILD,
            name = "autodelete",
            subcommand = "enable",
            description = "Enable message auto-deletion for a channel")
    public void enableCommand(@NotNull GuildSlashEvent event,
                                  @AppOption(description = "The channel to enable message auto-deletion for") @Nullable GuildChannel channel) {
        if (channel == null) channel = event.getChannel().asGuildMessageChannel();
        final Map<Long, Set<Long>> autoDeletes = cobalt.data.getGuild(event.getGuild()).autoDeletes;

        // Check if already enabled
        if (autoDeletes.containsKey(channel.getIdLong())) {
            event.reply("Message auto-deletion is already enabled for " + channel.getAsMention()).setEphemeral(true).queue();
            return;
        }

        // Enable auto-deletion
        autoDeletes.put(channel.getIdLong(), new HashSet<>());
        event.reply("Message auto-deletion is now enabled for " + channel.getAsMention()).setEphemeral(true).queue();
    }

    @JDASlashCommand(
            scope = CommandScope.GUILD,
            name = "autodelete",
            subcommand = "disable",
            description = "Disable message auto-deletion for a channel")
    public void disableCommand(@NotNull GuildSlashEvent event,
                                  @AppOption(description = "The channel to disable message auto-deletion for") @Nullable GuildChannel channel) {
        if (channel == null) channel = event.getChannel().asGuildMessageChannel();
        final Map<Long, Set<Long>> autoDeletes = cobalt.data.getGuild(event.getGuild()).autoDeletes;

        // Check if already disabled
        if (!autoDeletes.containsKey(channel.getIdLong())) {
            event.reply("Message auto-deletion is already disabled for " + channel.getAsMention()).setEphemeral(true).queue();
            return;
        }

        // Disable auto-deletion
        autoDeletes.remove(channel.getIdLong());
        event.reply("Message auto-deletion is now disabled for " + channel.getAsMention()).setEphemeral(true).queue();
    }

    @JDASlashCommand(
            scope = CommandScope.GUILD,
            name = "autodelete",
            subcommand = "list",
            description = "List all channels with message auto-deletion enabled")
    public void listCommand(@NotNull GuildSlashEvent event) {
        final Map<Long, Set<Long>> autoDeletes = cobalt.data.getGuild(event.getGuild()).autoDeletes;

        // Check if empty
        if (autoDeletes.isEmpty()) {
            event.reply("No channels have message auto-deletion enabled").setEphemeral(true).queue();
            return;
        }

        // Reply
        final StringBuilder builder = new StringBuilder();
        for (final Long id : autoDeletes.keySet()) builder.append("<#").append(id).append(">, ");
        event.reply(builder.substring(0, builder.length() - 2)).setEphemeral(true).queue();
    }

    @JDASlashCommand(
            scope = CommandScope.GUILD,
            name = "autodelete",
            group = "bypass",
            subcommand = "add",
            description = "Add a role to bypass message auto-deletion")
    public void bypassAddCommand(@NotNull GuildSlashEvent event,
                                 @AppOption(description = "The role to add to the bypass list") @NotNull Role role,
                                 @AppOption(description = "The channel to add the bypass role to") @Nullable GuildChannel channel) {
        if (channel == null) channel = event.getChannel().asGuildMessageChannel();
        final Set<Long> autoDelete = getAutoDelete(event, channel);
        if (autoDelete == null) return;

        // Check if already added
        if (autoDelete.contains(role.getIdLong())) {
            event.reply(role.getAsMention() + " is already in the bypass list for " + channel.getAsMention()).setEphemeral(true).queue();
            return;
        }

        // Add role
        autoDelete.add(role.getIdLong());
        event.reply(role.getAsMention() + " is now in the bypass list for " + channel.getAsMention()).setEphemeral(true).queue();
    }

    @JDASlashCommand(
            scope = CommandScope.GUILD,
            name = "autodelete",
            group = "bypass",
            subcommand = "remove",
            description = "Remove a role from the bypass list")
    public void bypassRemoveCommand(@NotNull GuildSlashEvent event,
                                 @AppOption(description = "The role to remove from the bypass list") @NotNull Role role,
                                 @AppOption(description = "The channel to remove the bypass role from") @Nullable GuildChannel channel) {
        if (channel == null) channel = event.getChannel().asGuildMessageChannel();
        final Set<Long> autoDelete = getAutoDelete(event, channel);
        if (autoDelete == null) return;

        // Check if not added
        if (!autoDelete.contains(role.getIdLong())) {
            event.reply(role.getAsMention() + " is not in the bypass list for " + channel.getAsMention()).setEphemeral(true).queue();
            return;
        }

        // Remove role
        autoDelete.remove(role.getIdLong());
        event.reply(role.getAsMention() + " is no longer in the bypass list for " + channel.getAsMention()).setEphemeral(true).queue();
    }

    @JDASlashCommand(
            scope = CommandScope.GUILD,
            name = "autodelete",
            group = "bypass",
            subcommand = "list",
            description = "List all roles in the bypass list")
    public void bypassListCommand(@NotNull GuildSlashEvent event,
                                  @AppOption(description = "The channel to list the bypass roles for") @Nullable GuildChannel channel) {
        if (channel == null) channel = event.getChannel().asGuildMessageChannel();
        final Set<Long> autoDelete = getAutoDelete(event, channel);
        if (autoDelete == null) return;

        // Check if empty
        if (autoDelete.isEmpty()) {
            event.reply("No roles are in the bypass list for " + channel.getAsMention()).setEphemeral(true).queue();
            return;
        }

        // Reply
        final StringBuilder builder = new StringBuilder();
        for (final Long id : autoDelete) builder.append("<@&").append(id).append(">").append(", ");
        event.reply(builder.substring(0, builder.length() - 2)).setEphemeral(true).queue();
    }

    private Set<Long> getAutoDelete(@NotNull GuildSlashEvent event, @NotNull GuildChannel channel) {
        final Set<Long> autoDelete = cobalt.data.getGuild(event.getGuild()).autoDeletes.get(channel.getIdLong());
        if (autoDelete == null) event.reply("Message auto-deletion is not enabled for " + channel.getAsMention()).setEphemeral(true).queue();
        return autoDelete;
    }
}

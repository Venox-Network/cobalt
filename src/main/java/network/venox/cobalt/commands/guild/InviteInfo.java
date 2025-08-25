package network.venox.cobalt.commands.guild;

import io.github.freya022.botcommands.api.commands.annotations.Command;
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand;
import io.github.freya022.botcommands.api.commands.application.CommandScope;
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashOption;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.TopLevelSlashCommandData;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Invite;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.interactions.InteractionHook;

import org.jetbrains.annotations.NotNull;

import xyz.srnyx.lazylibrary.LazyEmoji;
import xyz.srnyx.lazylibrary.utility.LazyUtilities;

import java.time.OffsetDateTime;


@Command
public class InviteInfo extends ApplicationCommand {
    @TopLevelSlashCommandData(scope = CommandScope.GUILD)
    @JDASlashCommand(
            name = "inviteinfo",
            description = "Get information about a specific invite")
    public void inviteInfo(@NotNull GuildSlashEvent event,
                           @SlashOption(description = "The invite code") @NotNull String code) {
        event.deferReply().queue();
        final InteractionHook hook = event.getHook();
        try {
            event.getGuild().retrieveInvites()
                    .flatMap(invites -> {
                        // Parse code
                        final String parsedCode = code
                                .replace("https://", "")
                                .replace("http://", "")
                                .replace("discord.gg/", "")
                                .replace("discord.com/invite/", "");

                        // Get invite
                        final Invite invite = invites.stream()
                                .filter(i -> i.getCode().equals(parsedCode))
                                .findFirst()
                                .orElse(null);
                        if (invite == null) return hook.editOriginal(LazyEmoji.NO + " Couldn't find an invite with the code `" + parsedCode + "`!");

                        // Reply
                        final StringBuilder reply = new StringBuilder("**Code:** `" + invite.getCode() + "`");
                        final User inviter = invite.getInviter();
                        if (inviter != null) reply.append("\n**Creator:** ").append(inviter.getAsMention());
                        final Invite.Channel channel = invite.getChannel();
                        if (channel != null) reply.append("\n**Channel:** <#").append(channel.getId()).append(">");
                        final int maxUses = invite.getMaxUses();
                        final OffsetDateTime created = invite.getTimeCreated();
                        final int maxAge = invite.getMaxAge();
                        reply
                                .append("\n**Uses:** ").append(invite.getUses()).append("/").append(maxUses != 0 ? maxUses : "∞")
                                .append("\n**Expires:** ").append(maxAge != 0 ? "<t:" + created.plusSeconds(maxAge).toEpochSecond() + ":R>" : "Never")
                                .append("\n**Created:** <t:").append(created.toEpochSecond()).append(":R>");
                        return hook.editOriginal(reply.toString()).setAllowedMentions(LazyUtilities.NO_MENTIONS);
                    })
                    .queue();
        } catch (final InsufficientPermissionException e) {
            hook.editOriginal(LazyEmoji.NO + " I need the **" + Permission.MANAGE_SERVER.getName() + "** permission to view invites in this server!").queue();
        }
    }
}

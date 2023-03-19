package network.venox.cobalt.commands.guild;

import com.freya02.botcommands.api.annotations.CommandMarker;
import com.freya02.botcommands.api.annotations.Dependency;
import com.freya02.botcommands.api.annotations.UserPermissions;
import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.CommandScope;
import com.freya02.botcommands.api.application.annotations.AppOption;
import com.freya02.botcommands.api.application.slash.GuildSlashEvent;
import com.freya02.botcommands.api.application.slash.annotations.JDASlashCommand;
import com.freya02.botcommands.api.application.slash.annotations.LongRange;
import com.freya02.botcommands.api.application.slash.autocomplete.annotations.AutocompletionHandler;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;

import network.venox.cobalt.Cobalt;
import network.venox.cobalt.data.CoGuild;
import network.venox.cobalt.utility.CoMapper;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


@CommandMarker @UserPermissions(Permission.NICKNAME_MANAGE)
public class NicknameCmd extends ApplicationCommand {
    @NotNull private static final String AC_REMOVE_NICKNAMES = "NicknameCmd.removeCommand.nicknames";

    @Dependency private Cobalt cobalt;

    @JDASlashCommand(
            scope = CommandScope.GUILD,
            name = "nickname",
            subcommand = "list",
            description = "List the current phrases in the nickname blacklist")
    public void listCommand(@NotNull GuildSlashEvent event) {
        // Get nicknames
        final Set<String> nicknames = cobalt.data.getGuild(event.getGuild()).nicknameBlacklist;
        if (nicknames.isEmpty()) {
            event.reply("The nickname blacklist is empty").setEphemeral(true).queue();
            return;
        }

        // Reply
        event.reply("**The current blacklist contains `" + nicknames.size() + "` nicknames:**\n`" + String.join("`, `", nicknames) + "`").setEphemeral(true).queue();
    }

    @JDASlashCommand(
            scope = CommandScope.GUILD,
            name = "nickname",
            subcommand = "add",
            description = "Add a list of phrases to the nickname blacklist")
    public void addCommand(@NotNull GuildSlashEvent event,
                          @AppOption(description = "The blacklisted nicknames to add") @NotNull String nicknames,
                          @AppOption(description = "The characters used to separate multiple nicknames") @Nullable String delimiter) {
        final Set<String> nicknameSet = getNicknames(nicknames, delimiter);

        // Add to database
        final CoGuild coGuild = cobalt.data.getGuild(event.getGuild());
        coGuild.nicknameBlacklist.addAll(nicknameSet);
        coGuild.checkMemberNicknames(nicknameSet);

        // Reply
        event.reply("Added `" + nicknameSet.size() + "` nicknames to the blacklist").setEphemeral(true).queue();
    }

    @JDASlashCommand(
            scope = CommandScope.GUILD,
            name = "nickname",
            subcommand = "remove",
            description = "Remove a list of phrases from the nickname blacklist")
    public void removeCommand(@NotNull GuildSlashEvent event,
                              @AppOption(description = "The blacklisted nicknames to remove", autocomplete = AC_REMOVE_NICKNAMES) @NotNull String nicknames,
                              @AppOption(description = "The characters used to separate multiple nicknames") @Nullable String delimiter) {
        final Set<String> nicknameSet = getNicknames(nicknames, delimiter);

        // Add to database
        cobalt.data.getGuild(event.getGuild()).nicknameBlacklist.removeAll(nicknameSet);

        // Reply
        event.reply("Removed `" + nicknameSet.size() + "` nicknames from the blacklist").setEphemeral(true).queue();
    }

    @JDASlashCommand(
            scope = CommandScope.GUILD,
            name = "nickname",
            subcommand = "set",
            description = "Set the nickname a user will get when they use a blacklisted phrase in their nickname")
    public void setCommand(@NotNull GuildSlashEvent event,
                           @AppOption(description = "The nickname you want to set") @Nullable String nickname) {
        if (nickname != null && nickname.length() > 32) {
            event.reply("The nickname can't be longer than 32 characters").setEphemeral(true).queue();
            return;
        }
        final Guild guild = event.getGuild();
        final CoGuild coGuild = cobalt.data.getGuild(guild);

        // Set moderatedNickname
        coGuild.moderatedNickname = nickname;

        // Reply
        event.reply("Set the moderated nickname to `" + coGuild.getModeratedNickname() + "` for **" + guild.getName() + "**").setEphemeral(true).queue();
    }

    @JDASlashCommand(
            scope = CommandScope.GUILD,
            name = "nickname",
            subcommand = "max",
            description = "Set the maximum length of a nickname")
    public void maxCommand(@NotNull GuildSlashEvent event,
                           @AppOption(description = "The maximum length of a nickname") @LongRange(from = 0, to = 31) long max) {
        final Integer maxInt = CoMapper.toInt(max);
        if (maxInt == null) return;

        // Set maxNicknameLength
        final CoGuild guild = cobalt.data.getGuild(event.getGuild());
        guild.maxNicknameLength = maxInt;
        if (maxInt != 0) guild.checkMemberNicknames(null);

        // Reply
        event.reply("Set the maximum nickname length to `" + maxInt + "`").setEphemeral(true).queue();
    }

    @JDASlashCommand(
            scope = CommandScope.GUILD,
            name = "nickname",
            subcommand = "ban",
            description = "Ban a user from changing their nickname")
    public void banCommand(@NotNull GuildSlashEvent event,
                           @AppOption(description = "The user to ban") @NotNull Member member) {
        cobalt.data.getGuild(event.getGuild()).bannedNicknameUsers.add(member.getIdLong());
        member.modifyNickname(null).queue();
        event.reply(member.getAsMention() + " can no longer change their nickname").setEphemeral(true).queue();
    }

    @JDASlashCommand(
            scope = CommandScope.GUILD,
            name = "nickname",
            subcommand = "unban",
            description = "Unban a user from changing their nickname")
    public void unbanCommand(@NotNull GuildSlashEvent event,
                             @AppOption(description = "The user to unban") @NotNull Member member) {
        cobalt.data.getGuild(event.getGuild()).bannedNicknameUsers.remove(member.getIdLong());
        event.reply(member.getAsMention() + " can now change their nickname again").setEphemeral(true).queue();
    }

    @JDASlashCommand(
            scope = CommandScope.GUILD,
            name = "nickname",
            group = "whitelist",
            subcommand = "list",
            description = "Lists the roles in the nickname whitelist")
    @UserPermissions(Permission.MANAGE_ROLES)
    public void whitelistListCommand(@NotNull GuildSlashEvent event) {
        // Get whitelist
        final Set<Long> whitelist = cobalt.data.getGuild(event.getGuild()).nicknameWhitelist;
        if (whitelist.isEmpty()) {
            event.reply("The nickname whitelist is empty").setEphemeral(true).queue();
            return;
        }

        // Reply
        final Guild guild = event.getGuild();
        final List<String> roles = whitelist.stream()
                .map(guild::getRoleById)
                .filter(Objects::nonNull)
                .map(IMentionable::getAsMention)
                .toList();
        event.reply("**The current whitelist contains `" + roles.size() + "` role(s):**\n" + String.join(", ", roles)).setEphemeral(true).queue();
    }

    @JDASlashCommand(
            scope = CommandScope.GUILD,
            name = "nickname",
            group = "whitelist",
            subcommand = "add",
            description = "Adds a role to the nickname whitelist")
    @UserPermissions(Permission.MANAGE_ROLES)
    public void whitelistAddCommand(@NotNull GuildSlashEvent event,
                                 @AppOption(description = "The roles to add to the whitelist") @NotNull Role role) {
        cobalt.data.getGuild(event.getGuild()).nicknameWhitelist.add(role.getIdLong());
        event.reply("Added " + role.getAsMention() + " to the nickname whitelist").setEphemeral(true).queue();
    }

    @JDASlashCommand(
            scope = CommandScope.GUILD,
            name = "nickname",
            group = "whitelist",
            subcommand = "remove",
            description = "Removes a role from the nickname whitelist")
    @UserPermissions(Permission.MANAGE_ROLES)
    public void whitelistRemoveCommand(@NotNull GuildSlashEvent event,
                                    @AppOption(description = "The roles to remove from the whitelist") @NotNull Role role) {
        cobalt.data.getGuild(event.getGuild()).nicknameWhitelist.remove(role.getIdLong());
        event.reply("Removed " + role.getAsMention() + " from the nickname whitelist").setEphemeral(true).queue();
    }

    @AutocompletionHandler(name = AC_REMOVE_NICKNAMES) @NotNull
    public List<String> acRemoveNicknames(@NotNull CommandAutoCompleteInteractionEvent event) {
        final Guild guild = event.getGuild();
        if (guild == null) return Collections.emptyList();
        return new ArrayList<>(cobalt.data.getGuild(event.getGuild()).nicknameBlacklist);
    }

    @NotNull
    private Set<String> getNicknames(@NotNull String nicknames, @Nullable String delimiter) {
        Set<String> nicknamesSet = Collections.singleton(nicknames);
        if (delimiter != null) nicknamesSet = Arrays.stream(nicknames.split(Pattern.quote(delimiter))).collect(Collectors.toSet());
        return nicknamesSet.stream()
                .map(nickname -> nickname.toLowerCase().trim())
                .collect(Collectors.toSet());
    }
}

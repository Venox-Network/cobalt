package network.venox.cobalt.commands;

import com.freya02.botcommands.api.annotations.CommandMarker;
import com.freya02.botcommands.api.annotations.Dependency;
import com.freya02.botcommands.api.annotations.UserPermissions;
import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.CommandScope;
import com.freya02.botcommands.api.application.annotations.AppOption;
import com.freya02.botcommands.api.application.slash.GuildSlashEvent;
import com.freya02.botcommands.api.application.slash.annotations.JDASlashCommand;
import com.freya02.botcommands.api.application.slash.autocomplete.annotations.AutocompletionHandler;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;

import network.venox.cobalt.Cobalt;
import network.venox.cobalt.data.CoGuild;

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
            subcommand = "add",
            description = "Add a list of phrases to the nickname blacklist")
    public void addCommand(@NotNull GuildSlashEvent event,
                          @AppOption(description = "The blacklisted nicknames to add") @NotNull String nicknames,
                          @AppOption(description = "The characters used to separate multiple nicknames") @Nullable String delimiter) {
        final Set<String> nicknameSet = getNicknames(nicknames, delimiter);

        // Add to database
        cobalt.data.getGuild(event.getGuild()).addNicknames(nicknameSet);

        // Reply
        event.reply("Added `" + nicknameSet.size() + "` nicknames to the blacklist").setEphemeral(true).queue();
    }

    @JDASlashCommand(
            scope = CommandScope.GUILD,
            name = "nickname",
            subcommand = "list",
            description = "List the current phrases in the nickname blacklist")
    public void listCommand(@NotNull GuildSlashEvent event) {
        final Set<String> nicknames = cobalt.data.getGuild(event.getGuild()).nicknameBlacklist;
        event.reply("**The current blacklist contains `" + nicknames.size() + "` nicknames:**\n`" + String.join("`, `", nicknames) + "`").setEphemeral(true).queue();
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
        final Guild guild = event.getGuild();
        final CoGuild coGuild = cobalt.data.getGuild(guild);

        // Set moderatedNickname
        coGuild.moderatedNickname = nickname;

        // Reply
        event.reply("Set the moderated nickname to `" + coGuild.getModeratedNickname() + "` for **" + guild.getName() + "**").setEphemeral(true).queue();
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

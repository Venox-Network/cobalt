package network.venox.cobalt.contexts;

import com.freya02.botcommands.api.annotations.CommandMarker;
import com.freya02.botcommands.api.annotations.Dependency;
import com.freya02.botcommands.api.annotations.UserPermissions;
import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.CommandScope;
import com.freya02.botcommands.api.application.context.annotations.JDAUserCommand;
import com.freya02.botcommands.api.application.context.user.GuildUserEvent;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;

import network.venox.cobalt.Cobalt;
import network.venox.cobalt.data.CoGuild;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Set;


@CommandMarker @UserPermissions(Permission.NICKNAME_MANAGE)
public class NicknameContext extends ApplicationCommand {
    @Dependency private Cobalt cobalt;

    @JDAUserCommand(
            scope = CommandScope.GUILD,
            name = "Blacklist nickname")
    public void nicknameContext(@NotNull GuildUserEvent event) {
        final Member member = event.getTargetMember();
        if (member == null) return;
        final String nickname = member.getEffectiveName().toLowerCase().trim();
        final CoGuild guild = cobalt.data.getGuild(event.getGuild());

        // Remove nickname
        final Set<String> nicknames = guild.nicknameBlacklist;
        if (nicknames.contains(nickname)) {
            nicknames.remove(nickname);
            event.reply("`" + nickname + "` has been **removed** from the nickname blacklist").setEphemeral(true).queue();
            return;
        }

        // Add nickname
        guild.nicknameBlacklist.add(nickname);
        guild.checkMemberNicknames(Collections.singleton(nickname));

        // Reply
        event.reply("`" + nickname + "` has been **added** to the nickname blacklist").setEphemeral(true).queue();
    }
}

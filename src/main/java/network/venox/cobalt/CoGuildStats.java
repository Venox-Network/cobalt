package network.venox.cobalt;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;

import org.jetbrains.annotations.NotNull;

import java.util.List;


public class CoGuildStats {
    public int memberCount;
    public int humanCount;

    public CoGuildStats(int memberCount, int humanCount) {
        this.memberCount = memberCount;
        this.humanCount = humanCount;
    }

    public CoGuildStats(@NotNull Guild guild) {
        final List<Member> members = guild.loadMembers().get();
        this.memberCount = members.size();
        this.humanCount = (int) members.stream()
                .filter(member -> !member.getUser().isBot())
                .count();
    }
}

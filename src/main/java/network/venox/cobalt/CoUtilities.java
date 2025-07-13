package network.venox.cobalt;

import com.freya02.botcommands.api.application.slash.GlobalSlashEvent;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.lazylibrary.LazyEmbed;
import xyz.srnyx.lazylibrary.utility.LazyMapper;
import xyz.srnyx.lazylibrary.utility.LazyUtilities;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;


public final class CoUtilities {
    @Nullable
    public static UserSnowflake getUserSnowflake(@NotNull Cobalt bot, @NotNull GlobalSlashEvent event, @NotNull String user) {
        final Optional<UserSnowflake> snowflake = LazyMapper.toUserSnowflake(user);
        if (snowflake.isEmpty()) {
            event.replyEmbeds(LazyEmbed.invalidArgument("user", user).build(bot)).setEphemeral(true).queue();
            return null;
        }
        return snowflake.get();
    }

    @NotNull
    public static List<Command.Choice> acGuildMembers(@NotNull CommandAutoCompleteInteractionEvent event) {
        final Guild guild = event.getGuild();
        return guild == null ? List.of() : LazyUtilities.sortChoicesFuzzy(event, guild.getMembers().stream()
                .filter(member -> !member.getUser().isBot())
                .map(member -> new Command.Choice(member.getUser().getAsTag(), member.getIdLong()))
                .sorted(Comparator.comparing(choice -> choice.getName().toLowerCase()))
                .toList());
    }

    private CoUtilities() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}

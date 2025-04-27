package network.venox.cobalt;

import com.freya02.botcommands.api.application.slash.GlobalSlashEvent;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.requests.RestAction;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.lazylibrary.utility.LazyMapper;
import xyz.srnyx.lazylibrary.utility.LazyUtilities;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;


public final class CoUtilities {
    @Nullable
    public static UserSnowflake getUserSnowflake(@NotNull Cobalt cobalt, @NotNull GlobalSlashEvent event, @NotNull String user) {
        final Optional<UserSnowflake> snowflake = LazyMapper.toUserSnowflake(user);
        if (snowflake.isEmpty()) {
            event.replyEmbeds(cobalt.embeds.invalidArgument(user)).setEphemeral(true).queue();
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

    @NotNull
    public static RestAction<Boolean> userTalkedOrMentionedRecently(long userId, @NotNull MessageChannel channel, @NotNull OffsetDateTime time) {
        return channel.getHistory()
                .retrievePast(50)
                .map(messages -> {
                    for (final Message message : messages) {
                        if (message.getTimeCreated().isBefore(time)) break;
                        if (message.getAuthor().getIdLong() == userId || message.getMentions().getUsers().stream().anyMatch(user -> userId == user.getIdLong())) return true;
                    }
                    return false;
                });
    }

    @NotNull
    public static List<String> dynamicReact(@NotNull Message message) {
        final List<String> emojis = new ArrayList<>();
        for (final String emojiString : message.getContentRaw().split("(?<=^|\\s)(<a?:\\w+:\\d+>|:\\w+:)(?=\\s|$)")) {
            if (emojiString.isEmpty()) continue;
            emojis.add(emojiString);
            message.addReaction(Emoji.fromFormatted(emojiString)).queue();
        }
        return emojis;
    }
    
    private CoUtilities() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}

package network.venox.cobalt.contexts;

import com.freya02.botcommands.api.annotations.CommandMarker;
import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.CommandScope;
import com.freya02.botcommands.api.application.context.annotations.JDAMessageCommand;
import com.freya02.botcommands.api.application.context.message.GuildMessageEvent;

import org.jetbrains.annotations.NotNull;


@CommandMarker
public class TtsContext extends ApplicationCommand {
    @JDAMessageCommand(
            scope = CommandScope.GUILD,
            name = "TTS")
    public void ttsContext(@NotNull GuildMessageEvent event) {
        event.reply("Not implemented yet!").setEphemeral(true).queue();

        /*
        final Guild guild = event.getGuild();
        final GuildVoiceState voiceState = event.getMember().getVoiceState();
        final GuildVoiceState selfVoiceState = guild.getSelfMember().getVoiceState();
        final String text = event.getTarget().getContentRaw();
        if (voiceState == null || selfVoiceState == null || text.isBlank()) {
            event.reply("Text cannot be empty!").setEphemeral(true).queue();
            return;
        }

        // Check if user is in a channel
        final AudioChannelUnion channel = voiceState.getChannel();
        if (channel == null) {
            event.reply("You are not in a voice channel!").setEphemeral(true).queue();
            return;
        }

        // Check if bot is already in a different channel
        final AudioChannelUnion selfChannel = selfVoiceState.getChannel();
        if (selfChannel != null && !selfChannel.equals(channel)) {
            event.reply("I'm already in another voice channel!").setEphemeral(true).queue();
            return;
        }

        // Join channel if not already in it
        final AudioManager manager = guild.getAudioManager();
        if (selfChannel == null) manager.openAudioConnection(channel);

        // Speak TODO
        */
    }
}

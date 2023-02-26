package network.venox.cobalt.commands.guild;

import com.freya02.botcommands.api.annotations.CommandMarker;
import com.freya02.botcommands.api.annotations.Dependency;
import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.CommandScope;
import com.freya02.botcommands.api.application.annotations.AppOption;
import com.freya02.botcommands.api.application.slash.GuildSlashEvent;
import com.freya02.botcommands.api.application.slash.annotations.JDASlashCommand;

import network.venox.cobalt.Cobalt;

import org.jetbrains.annotations.NotNull;


@CommandMarker
public class TtsCmd extends ApplicationCommand {
    @Dependency private Cobalt cobalt;

    @JDASlashCommand(
            scope = CommandScope.GUILD,
            name = "tts",
            description = "Speak in a voice channel using text-to-speech")
    public void ttsCommand(@NotNull GuildSlashEvent event,
                           @AppOption(description = "The text to speak") @NotNull String text) {
        event.reply("Not implemented yet!").setEphemeral(true).queue();

        /*
        final Guild guild = event.getGuild();
        final GuildVoiceState voiceState = event.getMember().getVoiceState();
        final GuildVoiceState selfVoiceState = guild.getSelfMember().getVoiceState();
        if (voiceState == null || selfVoiceState == null) return;

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

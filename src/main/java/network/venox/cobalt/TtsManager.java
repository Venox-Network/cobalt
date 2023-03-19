package network.venox.cobalt;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.event.TrackEndEvent;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame;

import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.VoiceManager;
import com.sun.speech.freetts.audio.SingleFileAudioPlayer;

import net.dv8tion.jda.api.audio.AudioSendHandler;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import net.dv8tion.jda.api.managers.AudioManager;

import network.venox.cobalt.utility.CoUtilities;

import org.jetbrains.annotations.NotNull;

import javax.sound.sampled.AudioFileFormat;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;


public class TtsManager {
    @NotNull private static final AudioPlayerManager playerManager = new DefaultAudioPlayerManager();
    @NotNull private static final LocalAudioSendHandler sendHandler = new LocalAudioSendHandler(playerManager.createPlayer());
    static {
        AudioSourceManagers.registerLocalSource(playerManager);
        sendHandler.player.addListener(event -> {
            if (event instanceof TrackEndEvent endEvent) CoUtilities.deleteFile(Path.of(endEvent.track.getInfo().uri), true);
        });
    }

    public static void speak(@NotNull CommandInteraction event, @NotNull String text) {
        final Member member = event.getMember();
        if (member == null) return;
        final Guild guild = member.getGuild();
        final GuildVoiceState voiceState = member.getVoiceState();
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

        // Check if text is empty
        if (text.isBlank()) {
            event.reply("Text cannot be empty!").setEphemeral(true).queue();
            return;
        }

        // Check if text is too long
        final int length = text.length();
        if (length > 500) {
            event.reply("Character count exceeded limit: **" + length + "**/500!").setEphemeral(true).queue();
            return;
        }
        
        // Check if already speaking
        if (sendHandler.player.getPlayingTrack() != null) {
            event.reply("I'm already speaking!").setEphemeral(true).queue();
            return;
        }

        // Reply
        event.reply("Saying this in " + channel.getAsMention() + ":\n> " + text).setAllowedMentions(List.of()).queue();

        // Create TTS directory
        final File directory = new File("tts");
        if (!directory.exists()) try {
            Files.createDirectory(directory.toPath());
        } catch (final IOException e) {
            e.printStackTrace();
        }

        // Create audio file
        final Voice voice = VoiceManager.getInstance().getVoice("kevin16");
        final com.sun.speech.freetts.audio.AudioPlayer ttsPlayer = new SingleFileAudioPlayer("tts/" + event.getIdLong(), AudioFileFormat.Type.WAVE);
        voice.setAudioPlayer(ttsPlayer);
        voice.allocate();
        voice.speak(text);
        voice.deallocate();
        ttsPlayer.close();

        // Join channel
        final AudioManager manager = guild.getAudioManager();
        manager.openAudioConnection(channel);
        manager.setSelfDeafened(true);
        manager.setSendingHandler(sendHandler);

        // Play audio file
        playerManager.loadItem("tts/" + event.getIdLong() + ".wav", new LocalAudioLoadResultHandler(sendHandler.player));
    }

    public static class LocalAudioSendHandler implements AudioSendHandler {
        @NotNull public final AudioPlayer player;
        private AudioFrame lastFrame;

        public LocalAudioSendHandler(@NotNull AudioPlayer player) {
            this.player = player;
        }

        @Override
        public boolean canProvide() {
            lastFrame = player.provide();
            return lastFrame != null;
        }

        @Override
        public ByteBuffer provide20MsAudio() {
            return ByteBuffer.wrap(lastFrame.getData());
        }

        @Override
        public boolean isOpus() {
            return true;
        }
    }

    public static class LocalAudioLoadResultHandler implements AudioLoadResultHandler {
        @NotNull private final AudioPlayer player;

        public LocalAudioLoadResultHandler(@NotNull AudioPlayer player) {
            this.player = player;
        }

        @Override
        public void trackLoaded(@NotNull AudioTrack track) {
            player.startTrack(track, false);
        }

        @Override
        public void playlistLoaded(@NotNull AudioPlaylist playlist) {
            // Do nothing
        }

        @Override
        public void noMatches() {
            // Do nothing
        }

        @Override
        public void loadFailed(@NotNull FriendlyException exception) {
            // Do nothing
        }
    }
}

package network.venox.cobalt.contexts;

import com.freya02.botcommands.api.annotations.CommandMarker;
import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.CommandScope;
import com.freya02.botcommands.api.application.context.annotations.JDAMessageCommand;
import com.freya02.botcommands.api.application.context.message.GlobalMessageEvent;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.interactions.InteractionHook;

import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;


@CommandMarker
public class PasteContext extends ApplicationCommand {
    @NotNull private static final String pasteUrl = "paste.venox.network";

    @JDAMessageCommand(
            scope = CommandScope.GLOBAL,
            name = "Upload paste")
    public void pasteContext(@NotNull GlobalMessageEvent event) {
        final Message message = event.getTarget();

        // Get the first attachment
        final Message.Attachment attachment = message.getAttachments().get(0);
        if (attachment == null || attachment.isImage() || attachment.isVideo()) {
            event.reply("Message must have a text file as the first attachment").setEphemeral(true).queue();
            return;
        }

        // Establish connection
        final URLConnection connection;
        try {
            connection = new URL("https://" + pasteUrl + "/documents").openConnection();
        } catch (final IOException e) {
            event.reply("Failed to upload paste").setEphemeral(true).queue();
            e.printStackTrace();
            return;
        }
        connection.setRequestProperty("authority", pasteUrl);
        connection.setRequestProperty("accept", "application/json, text/javascript, /; q=0.01");
        connection.setRequestProperty("x-requested-with", "XMLHttpRequest");
        connection.setRequestProperty("user-agent", event.getUser().getAsTag() + " via Cobalt");
        connection.setRequestProperty("content-type", "application/json; charset=UTF-8");
        connection.setDoOutput(true);

        // Write to connection
        try (final OutputStream stream = connection.getOutputStream()) {
            stream.write(readStream(attachment.getProxy().download().get()).getBytes());
            stream.flush();
        } catch (final IOException | InterruptedException | ExecutionException e) {
            event.reply("Failed to upload paste").setEphemeral(true).queue();
            e.printStackTrace();
            if (e instanceof InterruptedException) Thread.currentThread().interrupt();
            return;
        }

        // Reply with paste link
        final String fileName = attachment.getFileName();
        try {
            event.reply("Done!").setEphemeral(true)
                    .flatMap(InteractionHook::deleteOriginal)
                    .queue();
            message.reply("https://" + pasteUrl + "/" + readStream(connection.getInputStream()).split("\"")[3] + "." + fileName.substring(fileName.lastIndexOf('.') + 1)).queue();
        } catch (final IOException e) {
            event.reply("Failed to upload paste").setEphemeral(true).queue();
            e.printStackTrace();
        }
    }

    private String readStream(InputStream stream) {
        return new BufferedReader(new InputStreamReader(stream)).lines().collect(Collectors.joining("\n"));
    }
}

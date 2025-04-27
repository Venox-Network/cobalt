package network.venox.cobalt.components;

import com.freya02.botcommands.api.annotations.CommandMarker;
import com.freya02.botcommands.api.annotations.Dependency;
import com.freya02.botcommands.api.components.annotations.JDAButtonListener;
import com.freya02.botcommands.api.components.event.ButtonEvent;

import network.venox.cobalt.Cobalt;
import network.venox.cobalt.data.objects.CoModmail;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.srnyx.lazylibrary.LazyEmoji;


@CommandMarker
public class ModmailButtons {
    @Dependency private Cobalt cobalt;

    @JDAButtonListener(name = CoModmail.BUTTON_CLOSE_PRIVATE)
    public void closePrivate(@NotNull ButtonEvent event) {
        closeButton(event, cobalt.oldData.global.getModmailByUserId(event.getUser().getIdLong()));
    }

    @JDAButtonListener(name = CoModmail.BUTTON_CLOSE_THREAD)
    public void closeThread(@NotNull ButtonEvent event) {
        closeButton(event, cobalt.oldData.global.getModmailByThreadId(event.getChannel().getIdLong()));
    }

    @JDAButtonListener(name = CoModmail.BUTTON_DELETE)
    public void deleteThread(@NotNull ButtonEvent event) {
        final CoModmail modmail = cobalt.oldData.global.getModmailByThreadId(event.getChannel().getIdLong());
        if (modmail == null) {
            event.reply(LazyEmoji.NO + " This is not a modmail thread!").setEphemeral(true).queue();
            return;
        }
        modmail.delete(event.getUser());
    }

    private void closeButton(@NotNull ButtonEvent event, @Nullable CoModmail modmail) {
        if (modmail == null) {
            event.reply(LazyEmoji.NO + " This is not a modmail thread!").setEphemeral(true).queue();
            return;
        }
        modmail.close(event.getUser());
    }
}

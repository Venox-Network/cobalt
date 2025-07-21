package network.venox.cobalt.apps.message;

import com.freya02.botcommands.api.annotations.CommandMarker;
import com.freya02.botcommands.api.annotations.Dependency;
import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.CommandScope;
import com.freya02.botcommands.api.application.context.annotations.JDAMessageCommand;
import com.freya02.botcommands.api.application.context.message.GlobalMessageEvent;

import net.dv8tion.jda.api.entities.Message;

import network.venox.cobalt.Cobalt;

import network.venox.cobalt.mongo.ReactChannel;
import org.jetbrains.annotations.NotNull;

import xyz.srnyx.lazylibrary.LazyEmoji;

import java.util.List;


@CommandMarker
public class React extends ApplicationCommand {
    @Dependency private Cobalt bot;

}

package network.venox.cobalt.commands.global;

import com.freya02.botcommands.api.annotations.CommandMarker;
import com.freya02.botcommands.api.annotations.Dependency;
import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.CommandScope;
import com.freya02.botcommands.api.application.annotations.AppOption;
import com.freya02.botcommands.api.application.slash.GlobalSlashEvent;
import com.freya02.botcommands.api.application.slash.annotations.JDASlashCommand;
import com.freya02.botcommands.api.components.Components;
import com.freya02.botcommands.api.components.annotations.JDASelectionMenuListener;
import com.freya02.botcommands.api.components.event.StringSelectionEvent;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.dv8tion.jda.api.utils.data.DataObject;

import network.venox.cobalt.Cobalt;
import network.venox.cobalt.utility.CoUtilities;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.List;


@CommandMarker
public class EmbedCmd extends ApplicationCommand {
    @NotNull private static final String SM_EMBED = "EmbedCmd.embedCommand.embed";

    @Dependency private Cobalt cobalt;

    @JDASlashCommand(
            scope = CommandScope.GLOBAL,
            name = "embed",
            description = "Create an embed")
    public void embedCommand(@NotNull GlobalSlashEvent event,
                             @AppOption(description = "The JSON to use for the embed") @Nullable String json) {
        final MessageChannelUnion channelUnion = event.getChannel();
        final User user = event.getUser();
        final long userId = user.getIdLong();

        // Check if user is already creating an embed
        if (cobalt.embedBuilders.containsKey(userId)) {
            event.reply("You are already creating an embed!")
                    .addActionRow(Components.dangerButton(buttonEvent -> {
                        cobalt.embedBuilders.remove(userId);
                        buttonEvent.editMessage("Embed creation cancelled").queue();
                    }).build("Cancel"))
                    .setEphemeral(true)
                    .queue();
            return;
        }

        EmbedBuilder builder = new EmbedBuilder().setTitle("N/A");
        if (json != null) {
            final EmbedBuilder jsonBuilder = CoUtilities.getEmbedFromJson(json);
            if (jsonBuilder != null) builder = jsonBuilder;
        }
        final EmbedBuilder finalBuilder = builder;

        final RestAction<Message> action = event.getJDA().openPrivateChannelById(userId)
                .flatMap(channel -> channel.sendMessageEmbeds(finalBuilder.build())
                        .addActionRow(selectMenu())
                        .addActionRow(getButtons(cobalt, user, channelUnion.getIdLong())));

        // Command executed in a guild
        if (!channelUnion.getType().equals(ChannelType.PRIVATE)) {
            action.flatMap(message -> {
                cobalt.embedBuilders.put(userId, new Data(channelUnion.getIdLong(), message.getIdLong(), null, null));
                return event.reply(message.getJumpUrl()).setEphemeral(true);
            }).queue();
            return;
        }

        // Command executed in DMs
        action
                .flatMap(message -> {
                    cobalt.embedBuilders.put(userId, new Data(channelUnion.getIdLong(), message.getIdLong(), null, null));
                    return event.reply("Embed created!").setEphemeral(true);
                })
                .flatMap(InteractionHook::deleteOriginal)
                .queue();
    }

    @JDASelectionMenuListener(name = SM_EMBED)
    public void embedMenu(@NotNull StringSelectionEvent event) {
        final Data data = cobalt.embedBuilders.get(event.getUser().getIdLong());
        if (data == null) {
            event.reply("You are not currently creating an embed").setEphemeral(true).queue();
            return;
        }
        final String value = event.getValues().get(0);
        data.parameter = value;

        // Get format
        final String format = switch (value) {
            case "title", "author" -> "`text==url`";
            case "color" -> "`#hex`";
            case "footer" -> "`text==iconUrl`";
            case "thumbnail", "image" -> "`url`";
            case "field" -> "`name==value==inline` (`inline` either `true` or `false`)";
            case "timestamp" -> "`epochTime` or `now`";
            default -> "`text`";
        };

        // Reply
        event.reply("Please enter the **" + value + "** for the embed\n**Format:** " + format).queue(hook -> data.parameterHook = hook);
    }

    @NotNull
    public static StringSelectMenu selectMenu() {
        return Components.stringSelectionMenu(SM_EMBED)
                .addOption("Color", "color")
                .addOption("Author", "author")
                .addOption("Title", "title")
                .addOption("Description", "description")
                .addOption("Field", "field")
                .addOption("Thumbnail", "thumbnail")
                .addOption("Image", "image")
                .addOption("Footer", "footer")
                .addOption("Timestamp", "timestamp")
                .build();
    }

    @NotNull
    public static List<Button> getButtons(@NotNull Cobalt cobalt, @NotNull User user, long channelId) {
        final List<Button> buttons = new ArrayList<>();
        final long userId = user.getIdLong();
        buttons.add(Components.primaryButton(buttonEvent -> {
            final MessageEmbed embed = buttonEvent.getMessage().getEmbeds().get(0);
            final DataObject data = embed.toData();

            // Convert timestamp to milliseconds
            final OffsetDateTime timestamp = embed.getTimestamp();
            if (timestamp != null) data.put("timestamp", timestamp.getLong(ChronoField.INSTANT_SECONDS) * 1000 + timestamp.getLong(ChronoField.MILLI_OF_SECOND));

            // Reply
            buttonEvent.reply("```json\n" + data + "\n```").setEphemeral(true).queue();
        }).build("Export JSON"));
        if (cobalt.config.isOwner(user)) buttons.add(Components.successButton(buttonEvent -> {
            final MessageChannel channel = buttonEvent.getJDA().getChannelById(MessageChannel.class, channelId);
            if (channel == null) {
                buttonEvent.reply("Channel not found").setEphemeral(true).queue();
                return;
            }
            final MessageCreateAction action = channel.sendMessageEmbeds(buttonEvent.getMessage().getEmbeds());

            // Original command executed in a guild
            if (!channel.getType().equals(ChannelType.PRIVATE)) {
                action.queue(message -> buttonEvent.reply(message.getJumpUrl()).setEphemeral(true).queue());
                return;
            }

            // Original command executed in DMs
            action.queue(message -> buttonEvent.deferEdit().queue());
        }).build("Send"));
        buttons.add(Components.dangerButton(buttonEvent -> {
            cobalt.embedBuilders.remove(userId);
            buttonEvent.getMessage().delete().queue();
        }).build("Cancel"));
        return buttons;
    }

    public static final class Data {
        public final long channel;
        public long embedMessage;
        @Nullable public InteractionHook parameterHook;
        @Nullable public String parameter;

        public Data(long channel, long embedMessage, @Nullable InteractionHook parameterHook, @Nullable String parameter) {
            this.channel = channel;
            this.embedMessage = embedMessage;
            this.parameterHook = parameterHook;
            this.parameter = parameter;
        }

        @NotNull
        public RestAction<Message> getEmbedMessage(@NotNull PrivateChannel channel) {
            return channel.retrieveMessageById(embedMessage);
        }
    }
}

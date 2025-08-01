package network.venox.cobalt.commands.global;

import com.freya02.botcommands.api.annotations.CommandMarker;
import com.freya02.botcommands.api.annotations.Dependency;
import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.CommandScope;
import com.freya02.botcommands.api.application.annotations.AppOption;
import com.freya02.botcommands.api.application.context.annotations.JDAMessageCommand;
import com.freya02.botcommands.api.application.context.message.GlobalMessageEvent;
import com.freya02.botcommands.api.application.slash.GlobalSlashEvent;
import com.freya02.botcommands.api.application.slash.annotations.JDASlashCommand;
import com.freya02.botcommands.api.components.Components;
import com.freya02.botcommands.api.components.annotations.JDASelectionMenuListener;
import com.freya02.botcommands.api.components.event.StringSelectionEvent;

import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;
import net.dv8tion.jda.api.utils.messages.MessageEditData;

import network.venox.cobalt.Cobalt;
import network.venox.cobalt.mongo.CoUser;

import org.jetbrains.annotations.NotNull;

import space.dynomake.libretranslate.Language;
import space.dynomake.libretranslate.Translator;

import xyz.srnyx.javautilities.MapGenerator;

import xyz.srnyx.lazylibrary.LazyEmoji;

import java.util.List;
import java.util.Map;


@CommandMarker
public class Translate extends ApplicationCommand {
    @NotNull private static final String MENU_TRANSLATE_LANGUAGE = "TranslateMenu.translateLanguage";
    @NotNull public static final List<Language> LANGUAGES = List.of(
            Language.ENGLISH, Language.SPANISH, Language.FRENCH, Language.GERMAN, Language.TURKISH,
            Language.RUSSIAN, Language.DUTCH, Language.PORTUGUESE, Language.CHINESE, Language.JAPANESE,
            Language.KOREAN, Language.ITALIAN, Language.ARABIC, Language.POLISH, Language.UKRAINIAN,
            Language.HINDI, Language.GREEK, Language.IRISH, Language.INDONESIAN, Language.CZECH,
            Language.SWEDISH, Language.FINNISH, Language.DANISH, Language.HEBREW, Language.PERSIAN);
    @NotNull private static final Map<Language, Emoji> LANGUAGE_EMOJIS = MapGenerator.HASH_MAP.mapOf(
            LANGUAGES,
            List.of(Emoji.fromUnicode("🇬🇧"), Emoji.fromUnicode("🇪🇸"), Emoji.fromUnicode("🇫🇷"), Emoji.fromUnicode("🇩🇪"), Emoji.fromUnicode("🇹🇷"),
                    Emoji.fromUnicode("🇷🇺"), Emoji.fromUnicode("🇳🇱"), Emoji.fromUnicode("🇵🇹"), Emoji.fromUnicode("🇨🇳"), Emoji.fromUnicode("🇯🇵"),
                    Emoji.fromUnicode("🇰🇷"), Emoji.fromUnicode("🇮🇹"), Emoji.fromUnicode("🇸🇦"), Emoji.fromUnicode("🇵🇱"), Emoji.fromUnicode("🇺🇦"),
                    Emoji.fromUnicode("🇮🇳"), Emoji.fromUnicode("🇬🇷"), Emoji.fromUnicode("🇮🇪"), Emoji.fromUnicode("🇮🇩"), Emoji.fromUnicode("🇨🇿"),
                    Emoji.fromUnicode("🇸🇪"), Emoji.fromUnicode("🇫🇮"), Emoji.fromUnicode("🇩🇰"), Emoji.fromUnicode("🇮🇱"), Emoji.fromUnicode("🇮🇷")));
    @NotNull private static final List<SelectOption> LANGUAGE_OPTIONS = LANGUAGES.stream()
            .map(language -> {
                final String name = language.name();
                return SelectOption.of(name.charAt(0) + name.substring(1).toLowerCase(), name).withEmoji(LANGUAGE_EMOJIS.get(language));
            })
            .toList();

    @Dependency private Cobalt bot;

    @JDASlashCommand(
            scope = CommandScope.GLOBAL,
            name = "translate",
            description = "Translate a message to another language")
    public void translateCommand(@NotNull GlobalSlashEvent event,
                                 @AppOption(description = "The message to translate") @NotNull String message) {
        event.deferReply(true).queue();
        event.getHook().editOriginal(getMessage(message, bot.mongo.getMagicCollection(CoUser.class)
                .findOne("_id", event.getUser().getIdLong())
                .map(user -> user.language)
                .orElse(Language.ENGLISH))).queue();
    }

    @JDAMessageCommand(
            scope = CommandScope.GLOBAL,
            name = "Translate")
    public void translateContext(@NotNull GlobalMessageEvent event) {
        final String message = event.getTarget().getContentStripped();
        if (message.isEmpty()) {
            event.reply(LazyEmoji.NO + " Message cannot be empty!").setEphemeral(true).queue();
            return;
        }
        event.deferReply(true).queue();

        // Get user's language
        final Language language = bot.mongo.getMagicCollection(CoUser.class).findOne("_id", event.getUser().getIdLong())
                .map(coUser -> coUser.language)
                .orElse(Language.ENGLISH);

        // Translate and reply
        event.getHook().editOriginal(getMessage(message, language)).queue();
    }

    @JDASelectionMenuListener(name = MENU_TRANSLATE_LANGUAGE)
    public void menuTranslateLanguage(@NotNull StringSelectionEvent event,
                                      @AppOption String message) {
        event.deferEdit()
                .flatMap(hook -> hook.editOriginal(getMessage(message, Language.valueOf(event.getValues().getFirst()))))
                .queue();
    }

    @NotNull
    private static MessageEditData getMessage(@NotNull String message, @NotNull Language language) {
        final MessageEditBuilder builder = new MessageEditBuilder()
                .setActionRow(Components.stringSelectionMenu(MENU_TRANSLATE_LANGUAGE, message)
                        .oneUse()
                        .setPlaceholder("Select a language")
                        .addOptions(LANGUAGE_OPTIONS)
                        .setDefaultValues(language.name()).build());

        // Translate message
        String translatedMessage;
        try {
            translatedMessage = Translator.translate(language, message);
        } catch (final Exception e) {
            return builder
                    .setContent(LazyEmoji.NO + " **Translation failed!** Please try again later...")
                    .build();
        }

        // Reply
        if (translatedMessage.length() > 2000) translatedMessage = Translator.translate(Language.ENGLISH, language, "*Translation too long!*");
        return builder
                .setContent(translatedMessage)
                .build();
    }
}

package network.venox.cobalt.components;

import com.freya02.botcommands.api.application.annotations.AppOption;
import com.freya02.botcommands.api.components.Components;
import com.freya02.botcommands.api.components.annotations.JDASelectionMenuListener;
import com.freya02.botcommands.api.components.event.StringSelectionEvent;

import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;
import net.dv8tion.jda.api.utils.messages.MessageEditData;

import org.jetbrains.annotations.NotNull;

import space.dynomake.libretranslate.Language;
import space.dynomake.libretranslate.Translator;

import xyz.srnyx.javautilities.MapGenerator;

import java.util.List;
import java.util.Map;


public class TranslateMenu {
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

    @JDASelectionMenuListener(name = MENU_TRANSLATE_LANGUAGE)
    public void menuTranslateLanguage(@NotNull StringSelectionEvent event,
                                      @AppOption String message) {
        event.deferEdit()
                .flatMap(hook -> hook.editOriginal(getMessage(message, Language.valueOf(event.getValues().getFirst()))))
                .queue();
    }

    @NotNull
    public static MessageEditData getMessage(@NotNull String message, @NotNull Language language) {
        String translatedMessage = Translator.translate(language, message);
        if (translatedMessage.length() > 2000) translatedMessage = Translator.translate(Language.ENGLISH, language, "*Translation too long!*");
        return new MessageEditBuilder()
                .setContent(translatedMessage)
                .setActionRow(Components.stringSelectionMenu(MENU_TRANSLATE_LANGUAGE, message)
                        .oneUse()
                        .setPlaceholder("Select a language")
                        .addOptions(LANGUAGE_OPTIONS)
                        .setDefaultValues(language.name()).build())
                .build();
    }
}

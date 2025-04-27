package network.venox.cobalt.components;

import com.freya02.botcommands.api.application.annotations.AppOption;
import com.freya02.botcommands.api.components.Components;
import com.freya02.botcommands.api.components.annotations.JDASelectionMenuListener;
import com.freya02.botcommands.api.components.event.StringSelectionEvent;

import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;

import net.suuft.libretranslate.Language;
import net.suuft.libretranslate.Translator;

import org.jetbrains.annotations.NotNull;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;


public class TranslateMenu {
    @NotNull private static final String SM_TRANSLATE_LANGUAGE = "TranslateContext.translateContext.language";

    @NotNull public static final List<Language> LANGUAGES = List.of(Language.ENGLISH,
            Language.SPANISH, Language.FRENCH, Language.GERMAN, Language.TURKISH,
            Language.RUSSIAN, Language.DUTCH, Language.PORTUGUESE, Language.CHINESE,
            Language.JAPANESE, Language.KOREAN, Language.ITALIAN, Language.ARABIC,
            Language.POLISH, Language.UKRAINIAN, Language.HINDI, Language.GREEK,
            Language.IRISH, Language.INDONESIAN, Language.CZECH, Language.SWEDISH,
            Language.FINNISH, Language.DANISH, Language.HEBREW, Language.PERSIAN);
    @NotNull private static final Map<Language, Emoji> LANGUAGE_EMOJIS = new EnumMap<>(Language.class);
    static {
        LANGUAGE_EMOJIS.put(Language.ENGLISH, Emoji.fromUnicode("🇬🇧"));
        LANGUAGE_EMOJIS.put(Language.SPANISH, Emoji.fromUnicode("🇪🇸"));
        LANGUAGE_EMOJIS.put(Language.FRENCH, Emoji.fromUnicode("🇫🇷"));
        LANGUAGE_EMOJIS.put(Language.GERMAN, Emoji.fromUnicode("🇩🇪"));
        LANGUAGE_EMOJIS.put(Language.TURKISH, Emoji.fromUnicode("🇹🇷"));
        LANGUAGE_EMOJIS.put(Language.RUSSIAN, Emoji.fromUnicode("🇷🇺"));
        LANGUAGE_EMOJIS.put(Language.DUTCH, Emoji.fromUnicode("🇳🇱"));
        LANGUAGE_EMOJIS.put(Language.PORTUGUESE, Emoji.fromUnicode("🇵🇹"));
        LANGUAGE_EMOJIS.put(Language.CHINESE, Emoji.fromUnicode("🇨🇳"));
        LANGUAGE_EMOJIS.put(Language.JAPANESE, Emoji.fromUnicode("🇯🇵"));
        LANGUAGE_EMOJIS.put(Language.KOREAN, Emoji.fromUnicode("🇰🇷"));
        LANGUAGE_EMOJIS.put(Language.ITALIAN, Emoji.fromUnicode("🇮🇹"));
        LANGUAGE_EMOJIS.put(Language.ARABIC, Emoji.fromUnicode("🇸🇦"));
        LANGUAGE_EMOJIS.put(Language.POLISH, Emoji.fromUnicode("🇵🇱"));
        LANGUAGE_EMOJIS.put(Language.UKRAINIAN, Emoji.fromUnicode("🇺🇦"));
        LANGUAGE_EMOJIS.put(Language.HINDI, Emoji.fromUnicode("🇮🇳"));
        LANGUAGE_EMOJIS.put(Language.GREEK, Emoji.fromUnicode("🇬🇷"));
        LANGUAGE_EMOJIS.put(Language.IRISH, Emoji.fromUnicode("🇮🇪"));
        LANGUAGE_EMOJIS.put(Language.INDONESIAN, Emoji.fromUnicode("🇮🇩"));
        LANGUAGE_EMOJIS.put(Language.CZECH, Emoji.fromUnicode("🇨🇿"));
        LANGUAGE_EMOJIS.put(Language.SWEDISH, Emoji.fromUnicode("🇸🇪"));
        LANGUAGE_EMOJIS.put(Language.FINNISH, Emoji.fromUnicode("🇫🇮"));
        LANGUAGE_EMOJIS.put(Language.DANISH, Emoji.fromUnicode("🇩🇰"));
        LANGUAGE_EMOJIS.put(Language.HEBREW, Emoji.fromUnicode("🇮🇱"));
        LANGUAGE_EMOJIS.put(Language.PERSIAN, Emoji.fromUnicode("🇮🇷"));
    }
    @NotNull private static final List<SelectOption> LANGUAGE_OPTIONS = LANGUAGES.stream()
            .map(language -> {
                final String name = language.name();
                return SelectOption.of(name.charAt(0) + name.substring(1).toLowerCase(), name).withEmoji(LANGUAGE_EMOJIS.get(language));
            })
            .toList();

    @JDASelectionMenuListener(name = SM_TRANSLATE_LANGUAGE)
    public void smTranslateLanguage(@NotNull StringSelectionEvent event,
                                    @AppOption String message) {
        event.deferEdit().queue();
        final TranslateMessage translateMessage = getMessage(message, Language.valueOf(event.getValues().get(0)));
        event.getHook().editOriginal(translateMessage.message)
                .setComponents(translateMessage.actionRow)
                .queue();
    }

    @NotNull
    public static TranslateMessage getMessage(@NotNull String message, @NotNull Language language) {
        String translatedMessage = Translator.translate(language, message);
        if (translatedMessage.length() > 2000) translatedMessage = Translator.translate(Language.ENGLISH, language, "*Translation too long!*");
        return new TranslateMessage(translatedMessage, ActionRow.of(Components.stringSelectionMenu(SM_TRANSLATE_LANGUAGE, message)
                .oneUse()
                .setPlaceholder("Select a language")
                .addOptions(LANGUAGE_OPTIONS)
                .setDefaultValues(language.name()).build()));
    }

    public record TranslateMessage(@NotNull String message, @NotNull ActionRow actionRow) {}
}

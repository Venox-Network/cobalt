package network.venox.cobalt.contexts;

import com.freya02.botcommands.api.annotations.CommandMarker;
import com.freya02.botcommands.api.annotations.Dependency;
import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.CommandScope;
import com.freya02.botcommands.api.application.annotations.AppOption;
import com.freya02.botcommands.api.application.context.annotations.JDAMessageCommand;
import com.freya02.botcommands.api.application.context.message.GlobalMessageEvent;
import com.freya02.botcommands.api.components.Components;
import com.freya02.botcommands.api.components.annotations.JDASelectionMenuListener;
import com.freya02.botcommands.api.components.event.StringSelectionEvent;

import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;

import net.suuft.libretranslate.Language;
import net.suuft.libretranslate.Translator;

import network.venox.cobalt.Cobalt;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;


@CommandMarker
public class TranslateContext extends ApplicationCommand {
    @NotNull private static final String SM_TRANSLATE_LANGUAGE = "TranslateContext.translateContext.language";
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
    @NotNull private static final List<SelectOption> LANGUAGE_OPTIONS = Stream.of(Language.ENGLISH,
                    Language.SPANISH, Language.FRENCH, Language.GERMAN, Language.TURKISH,
                    Language.RUSSIAN, Language.DUTCH, Language.PORTUGUESE, Language.CHINESE,
                    Language.JAPANESE, Language.KOREAN, Language.ITALIAN, Language.ARABIC,
                    Language.POLISH, Language.UKRAINIAN, Language.HINDI, Language.GREEK,
                    Language.IRISH, Language.INDONESIAN, Language.CZECH, Language.SWEDISH,
                    Language.FINNISH, Language.DANISH, Language.HEBREW, Language.PERSIAN)
            .map(language -> {
                final String name = language.name();
                return SelectOption.of(name.charAt(0) + name.substring(1).toLowerCase(), name).withEmoji(LANGUAGE_EMOJIS.get(language));
            })
            .toList();

    @Dependency private Cobalt cobalt;

    @JDAMessageCommand(
            scope = CommandScope.GLOBAL,
            name = "Translate")
    public void translateContext(@NotNull GlobalMessageEvent event) {
        final String message = event.getTarget().getContentRaw();
        if (message.isEmpty()) {
            event.reply("Message cannot be empty!").setEphemeral(true).queue();
            return;
        }
        event.replyComponents(getActionRow(message, null)).setEphemeral(true).queue();
    }

    @JDASelectionMenuListener(name = SM_TRANSLATE_LANGUAGE)
    public void smTranslateLanguage(@NotNull StringSelectionEvent event,
                                    @AppOption String message) {
        event.deferEdit().queue();
        final String language = event.getValues().get(0);

        // Translate message
        String translatedMessage = Translator.translate(Language.valueOf(language), message);
        if (translatedMessage.length() > 2000) translatedMessage = Translator.translate(Language.ENGLISH, Language.valueOf(language), "*Translation too long!*");

        // Edit original message
        event.getHook().editOriginal(translatedMessage)
                .setComponents(getActionRow(message, language))
                .queue();
    }

    @NotNull
    private ActionRow getActionRow(@NotNull String message, @Nullable String language) {
        final StringSelectMenu.Builder builder = Components.stringSelectionMenu(SM_TRANSLATE_LANGUAGE, message)
                .oneUse()
                .setPlaceholder("Select a language")
                .addOptions(LANGUAGE_OPTIONS);
        if (language != null) builder.setDefaultValues(language);
        return ActionRow.of(builder.build());
    }
}

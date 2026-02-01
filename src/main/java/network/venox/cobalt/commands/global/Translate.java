package network.venox.cobalt.commands.global;

import io.github.freya022.botcommands.api.commands.annotations.Command;
import io.github.freya022.botcommands.api.commands.application.CommandScope;
import io.github.freya022.botcommands.api.commands.application.context.annotations.JDAMessageCommand;
import io.github.freya022.botcommands.api.commands.application.context.message.GlobalMessageEvent;
import io.github.freya022.botcommands.api.commands.application.slash.GlobalSlashEvent;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashOption;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.TopLevelSlashCommandData;
import io.github.freya022.botcommands.api.components.SelectMenus;
import io.github.freya022.botcommands.api.components.annotations.ComponentData;
import io.github.freya022.botcommands.api.components.annotations.JDASelectMenuListener;
import io.github.freya022.botcommands.api.components.event.StringSelectEvent;

import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.selections.SelectOption;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;
import net.dv8tion.jda.api.utils.messages.MessageEditData;

import network.venox.cobalt.MongoProvider;
import network.venox.cobalt.mongo.CoUser;

import org.jetbrains.annotations.NotNull;

import space.dynomake.libretranslate.Language;
import space.dynomake.libretranslate.Translator;

import xyz.srnyx.javautilities.MapGenerator;

import xyz.srnyx.lazylibrary.LazyEmoji;

import java.util.List;
import java.util.Map;


@Command
public class Translate {
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

    @NotNull private final MongoProvider mongo;
    @NotNull private final SelectMenus selectMenus;

    public Translate(@NotNull MongoProvider mongo, @NotNull SelectMenus selectMenus) {
        this.mongo = mongo;
        this.selectMenus = selectMenus;
    }

    @TopLevelSlashCommandData(scope = CommandScope.GLOBAL)
    @JDASlashCommand(
            name = "translate",
            description = "Translate a message to another language")
    public void translateCommand(@NotNull GlobalSlashEvent event,
                                 @SlashOption(description = "The message to translate") @NotNull String message) {
        event.deferReply(true).queue();
        event.getHook().editOriginal(getMessage(message, mongo.database.getMagicCollection(CoUser.class)
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
        final Language language = mongo.database.getMagicCollection(CoUser.class).findOne("_id", event.getUser().getIdLong())
                .map(coUser -> coUser.language)
                .orElse(Language.ENGLISH);

        // Translate and reply
        event.getHook().editOriginal(getMessage(message, language)).queue();
    }

    @JDASelectMenuListener(MENU_TRANSLATE_LANGUAGE)
    public void menuTranslateLanguage(@NotNull StringSelectEvent event,
                                      @ComponentData String message) {
        event.deferEdit()
                .flatMap(hook -> hook.editOriginal(getMessage(message, Language.valueOf(event.getValues().getFirst()))))
                .queue();
    }

    @NotNull
    private MessageEditData getMessage(@NotNull String message, @NotNull Language language) {
        final MessageEditBuilder builder = new MessageEditBuilder()
                .setComponents(ActionRow.of(selectMenus.stringSelectMenu().persistent()
                        .bindTo(MENU_TRANSLATE_LANGUAGE, message)
                        .singleUse(true)
                        .setPlaceholder("Select a language")
                        .addOptions(LANGUAGE_OPTIONS)
                        .setDefaultValues(language.name()).build()));

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

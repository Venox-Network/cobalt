package network.venox.cobalt.data;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;

import network.venox.cobalt.Cobalt;
import network.venox.cobalt.utility.CoMapper;

import org.jetbrains.annotations.NotNull;

import org.spongepowered.configurate.serialize.SerializationException;

import java.io.File;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;


public class CoData {
    @NotNull private final Cobalt cobalt;
    public CoGlobal global;
    @NotNull public Set<CoGuild> guilds = new HashSet<>();
    @NotNull public Set<CoUser> users = new HashSet<>();

    public CoData(@NotNull Cobalt cobalt) {
        this.cobalt = cobalt;
        load();
    }

    public void load() {
        // global
        this.global = new CoGlobal(cobalt.jda);

        // guilds
        this.guilds = new HashSet<>();
        final File[] guildFiles = new File("data/guilds").listFiles();
        if (guildFiles != null) Arrays.stream(guildFiles)
                .map(file -> {
                    final Long id = CoMapper.toLong(file.getName().replace(".yml", ""));
                    if (id == null) return null;
                    return new CoGuild(cobalt, id);
                })
                .filter(Objects::nonNull)
                .forEach(guilds::add);

        // users
        this.users = new HashSet<>();
        final File[] userFiles = new File("data/users").listFiles();
        if (userFiles != null) Arrays.stream(userFiles)
                .map(file -> {
                    final Long id = CoMapper.toLong(file.getName().replace(".yml", ""));
                    if (id == null) return null;
                    return new CoUser(cobalt.jda, id);
                })
                .filter(Objects::nonNull)
                .forEach(users::add);
    }

    public void save() {
        // global
        try {
            global.save();
        } catch (final SerializationException e) {
            e.printStackTrace();
        }

        // guilds
        guilds.forEach(guild -> {
            try {
                guild.save();
            } catch (final SerializationException e) {
                e.printStackTrace();
            }
        });

        // users
        users.forEach(user -> {
            try {
                user.save();
            } catch (final SerializationException e) {
                e.printStackTrace();
            }
        });

        
        // Delete empty folders
        try {
            Files.deleteIfExists(new File("data/guilds").toPath());
            Files.deleteIfExists(new File("data/users").toPath());
        } catch (final IOException e) {
            if (!(e instanceof DirectoryNotEmptyException) && !(e instanceof AccessDeniedException)) e.printStackTrace();
        }
    }

    @NotNull
    public Set<CoGuild> loadGuilds() {
        cobalt.jda.getGuilds().forEach(this::getGuild);
        return guilds;
    }

    @NotNull
    public CoGuild getGuild(@NotNull Guild guild) {
        // Get existing guild
        final long id = guild.getIdLong();
        final CoGuild coGuild = guilds.stream()
                .filter(g -> g.guildId == id)
                .findFirst()
                .orElse(null);
        if (coGuild != null) return coGuild;

        // Create new guild
        final CoGuild newGuild = new CoGuild(cobalt, guild);
        guilds.add(newGuild);
        return newGuild;
    }

    @NotNull
    public CoUser getUser(@NotNull User user) {
        final long id = user.getIdLong();
        final CoUser coUser = users.stream()
                .filter(u -> u.userId == id)
                .findFirst()
                .orElse(null);
        if (coUser != null) return coUser;

        // Create new user
        final CoUser newUser = new CoUser(user);
        users.add(newUser);
        return newUser;
    }
}

const { glob } = require("glob");
const { promisify } = require("util");
const { Client } = require("discord.js");
const express = require('express');
const app = express();
const Discord = require("discord.js");
const bot = new Discord.Client({
    intents: 32767,
});

const globPromise = promisify(glob);

/**
 * @param {Client} bot
 */
module.exports = async (bot) => {
    // Commands
    const commandFiles = await globPromise(`${process.cwd()}/commands/**/*.js`);
    commandFiles.map((value) => {
        const file = require(value);
        const splitted = value.split("/");
        const directory = splitted[splitted.length - 2];

        if (file.name) {
            const properties = { directory, ...file };
            bot.commands.set(file.name, properties);
        }
    });

    // Events
    const eventFiles = await globPromise(`${process.cwd()}/events/*.js`);
    eventFiles.map((value) => require(value));

    // Slash Commands
    const slashCommands = await globPromise(
        `${process.cwd()}/SlashCommands/*/*.js`
    );

    const arrayOfSlashCommands = [];
    slashCommands.map((value) => {
        const file = require(value);
        if (!file?.name) return;
        bot.slashCommands.set(file.name, file);

        if (["MESSAGE", "USER"].includes(file.type)) delete file.description;
        arrayOfSlashCommands.push(file);
    });
    bot.on("ready", async () => {
        // Register for a single guild
        await bot.guilds.cache
            .get("replace this with your guild id")
            .commands.set(arrayOfSlashCommands);

        // Register for all the guilds the bot is in
        // await bot.application.commands.set(arrayOfSlashCommands);
    });

};

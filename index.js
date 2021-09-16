const { Client, Collection } = require("discord.js");

const Discord = require("discord.js");
const bot = new Discord.Client({
    intents: 32767,
});
module.exports = bot;

// Global Variables
bot.commands = new Collection();
bot.slashCommands = new Collection();
bot.config = require("./config.json");

// Initializing the project
require("./handler")(bot);

bot.login(bot.config.token);

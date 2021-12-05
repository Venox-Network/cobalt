const { Client, Collection } = require("discord.js");
const Discord = require("discord.js");
/*
const client = new Client({
    intents: 32767,
});*/
const client = new Discord.Client({
  intents: [
    "GUILDS",
    "GUILD_MESSAGES",
    "GUILD_INTEGRATIONS",
    /*"GUILD_MEMBERS", "GUILD_PRESENCES",*/ "GUILD_WEBHOOKS",
    "GUILD_MESSAGE_REACTIONS",
    "GUILD_MESSAGE_TYPING",
    "GUILD_VOICE_STATES",
  ],
});
module.exports = client;

// Global Variables
client.commands = new Collection();
client.slashCommands = new Collection();
client.config = require("./config.json");

// Initializing the project
require("./handler")(client);

// Status
client.on("ready", () => {
  client.user.setPresence({
    status: "online",
    activity: {
      name: "sudoku",
      type: "PLAYING",
    },
  });

  console.log(
    `${client.user.username} boutta' watch ${client.users.cache.size} users and ${client.guilds.cache.size} servers!`
  );
});

client.login(client.config.token);

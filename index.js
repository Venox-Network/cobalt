const { Client, Collection } = require("discord.js");
const Discord = require("discord.js");
/*
const client = new Client({
    intents: 32767,
});*/
const client = new Discord.Client({ intents: ["GUILDS", "GUILD_MESSAGES", "GUILD_INTEGRATIONS", /*"GUILD_MEMBERS", "GUILD_PRESENCES",*/ "GUILD_WEBHOOKS", "GUILD_MESSAGE_REACTIONS", "GUILD_MESSAGE_TYPING", "GUILD_VOICE_STATES"] });
module.exports = client;

// Global Variables
client.commands = new Collection();
client.slashCommands = new Collection();
client.config = require("./config.json");

// Initializing the project
require("./handler")(client);

// Status
client.on("ready", () => {
    client.user.setStatus("online");
  
    // this event means, that it will do something when the bot is online
    // i will log in the console that the bot is online when its online
    console.log(
      `${client.user.username} boutta' watch ${client.guilds.reduce(
        (prev, val) => val.memberCount + prev,
        0
      )} users and ${client.guilds.size} servers!`
    );
    // activity
  
    const status = [
      `${client.guilds.size} servers and ${client.users.size} users!`,
      'official server: dsc.gg/venoxnet',
  
  
    ]; // type here ur status how to do the variable ${bot.guilds.size}
    // it can be users too ok  ok
  
    setInterval(() => {
      client.user.setActivity(status.random(), { type: "WATCHING" });
    }, 10000);
  });

client.login(client.config.token);
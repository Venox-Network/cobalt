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
client.on("ready", async() => {
    
    const servers = await client.guilds.cache.size;
    const servercount = await client.guilds.cache.reduce((a,b) => a+b.memberCount, 0);

    client.user.setPresence({
        status: 'dnd',
        activity: {
           name: 'a video',
            type: 'WATCHING'
         }
      });

    /*
    const messages = [
        `Owned by srnyx & ChrizxzFTW`,
        `Join the network: dsc.gg/venoxnet`,
        `Watching ${servers} servers and ${servercount} members!`
    ]

    setInterval(() => {
        const status = messages[Math.floor(Math.random()*messages.length)]
        client.user.setActivity({ messages : [{name : `${status}`}]})
    }, 5000);
    */
  });

client.login(client.config.token);
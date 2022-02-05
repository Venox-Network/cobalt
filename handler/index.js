const { glob } = require("glob");
const mongoose = require("mongoose");
const { promisify } = require("util");
const { Client } = require("discord.js");
const express = require('express');
const app = express();
const Discord = require("discord.js");
const {Player} = require('discord-player');
const client = require("../index.js");
//const client = new Discord.Client({ ws: { intents: 32509 }});
/*const { Client, Intents } = require('discord.js');
const myIntents = new Intents();
myIntents.add(
    Intents.FLAGS.GUILD_PRESENCES, 
    Intents.FLAGS.GUILD_MEMBERS,
    Intents.FLAGS.GUILDS,
    Intents.FLAGS.GUILD_INTEGRATIONS,
    Intents.FLAGS.GUILD_WEBHOOKS,
    Intents.FLAGS.GUILD_MESSAGES,
    Intents.FLAGS.GUILD_MESSAGE_REACTIONS,
    Intents.FLAGS.GUILD_MESSAGE_TYPING
             );*/

//const client = new Discord.Client({ intents: ["GUILDS", "GUILD_MESSAGES", "GUILD_INTEGRATIONS", /*"GUILD_MEMBERS", "GUILD_PRESENCES",*/ "GUILD_WEBHOOKS", "GUILD_MESSAGE_REACTIONS", "GUILD_MESSAGE_TYPING"] }); //new Client({ intents: myIntents });

/*const bot = new Discord.Client({
    intents: 32767,
});
*/
const globPromise = promisify(glob);

/**
 * @param {Client} client
 */
module.exports = async(client) => {
    // Commands
    const commandFiles = await globPromise(`${process.cwd()}/slashCommands/**/*.js`);
    commandFiles.map((value) => {
        const file = require(value);
        const splitted = value.split("/");
        const directory = splitted[splitted.length - 2];

        if (file.name) {
            const properties = { directory, ...file };
            client.commands.set(file.name, properties);
        }
    });

    // Events
    const eventFiles = await globPromise(`${process.cwd()}/events/*.js`);
    eventFiles.map((value) => require(value));

    // Slash Commands
    const slashCommands = await globPromise(
        `${process.cwd()}/slashCommands/*/*.js`
    );

    const arrayOfSlashCommands = [];
    slashCommands.map((value) => {
        const file = require(value);
        if (!file?.name) return;
        client.slashCommands.set(file.name, file);

        if (["MESSAGE", "USER"].includes(file.type)) delete file.description;
        if (file.permissions) file.defaultPermissions = false;
        arrayOfSlashCommands.push(file);
    });
    client.on("ready", async() => {
        // Register for a single guild
      /*  const guild = client.guilds.cache.get("879734848946847774");
        await guild.commands.set(arrayOfSlashCommands).then((cmd) => {
          const getRoles = (commandName) => {
            const permissions = arrayOfSlashCommands.find(
              (x) => x.name === commandName
            ).userPermissions;

            if (!permissions) return null;
            return guild.roles.cache.filter(
              (x) => x.permissions.has(permissions) && !x.managed
            );
          };

          const fullPermissions = cmd.reduce((accumulator, x) => {
            const roles = getRoles(x.name);
            if (!roles) return accumulator;

            const permissions = roles.reduce((a,v) => {
              return [
                ...a,
                {
                  id:v.id,
                  type: "ROLE",
                  permissions: true,s
                },
              ]; 
            }, []);

            return [
              ...accumulator,
              {
                id: x.id,
                permissions,
              },
            ];

          }, [])
          
          guild.commands.permissions.set({ fullPermissions });

        });
        
        */


        // Register for all the guilds the client is in
        await client.application.commands.set(arrayOfSlashCommands);
    });

    // mongoose
    const { mongooseConnectionString } = require('../config.json')
    if (!mongooseConnectionString) return;

    mongoose.connect(mongooseConnectionString).then(() => console.log('Connected to mongodb'));

};

const player = require("../client/player");

player.on('queueEnd', queue => {
  queue.metadata.send('✅ | Queue finished');
});

player.on('channelEmpty', queue => {
  queue.metadata.send('❌ | Nobody is in the voice channel, leaving...');
});

//client.on("error", () => { client.login(token) });

/*

//const player = new Player(client);
const player = require("../../client/player");

player.on('error', (queue, error) => {
    console.log(`[${queue.guild.name}] Error emitted from the queue: ${error.message}`);
  });
  
  player.on('connectionError', (queue, error) => {
    console.log(`[${queue.guild.name}] Error emitted from the connection: ${error.message}`);
  });
  
  player.on('trackStart', (queue, track) => {
    queue.metadata.send(`▶ | Started playing: **${track.title}** in **${queue.connection.channel.name}**!`);
  });
  
  player.on('trackAdd', (queue, track) => {
    queue.metadata.send(`🎶 | Track **${track.title}** queued!`);
  });
  
  player.on('botDisconnect', queue => {
    queue.metadata.send('❌ | I was manually disconnected from the voice channel, clearing queue!');
  });
  
  player.on('channelEmpty', queue => {
    queue.metadata.send('❌ | Nobody is in the voice channel, leaving...');
  });
*/
/*
  client.on('voiceStateUpdate', (oldState, newState) => {

    // if nobody left the channel in question, return.
    if (oldState.channelID !==  oldState.guild.me.voice.channelID || newState.channel)
      return;
  
    // otherwise, check how many people are in the channel now
    if (!oldState.channel.members.size - 1) 
      setTimeout(() => { // if 1 (you), wait five minutes
        if (!oldState.channel.members.size - 1) // if there's still 1 member, 
           oldState.channel.leave(); // leave
       }, 300000); // (5 min in ms)
  });
  

  const db = require('quick.db')
*/
//under if(message.author.bot)
/*
client.on('message', async (message) =>{
if(db.has(`afk-${message.author.id}+${message.guild.id}`)) {
        const info = db.get(`afk-${message.author.id}+${message.guild.id}`)
        await db.delete(`afk-${message.author.id}+${message.guild.id}`)
        message.reply(`Your afk status have been removed (${info})`)
    }
    //checking for mentions
    if(message.mentions.members.first()) {
        if(db.has(`afk-${message.mentions.members.first().id}+${message.guild.id}`)) {
            message.channel.send(message.mentions.members.first().user.tag + ":" + db.get(`afk-${message.mentions.members.first().id}+${message.guild.id}`))
        }else return;
    }else;
  });
  */
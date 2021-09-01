
Array.prototype.random = function() {
  return this[Math.floor(Math.random() * this.length)];
};

const express = require('express');
//const Canvas = require('canvas');
const app = express();
//const keepAlive = require("./keepalive.js");
const auth = require('./auth.json');
const Discord = require("discord.js");
const bot = new Discord.Client({ intents: ["GUILDS", "GUILD_MESSAGES", "GUILD_INTEGRATIONS", "GUILD_MEMBERS", "GUILD_WEBHOOKS", "GUILD_PRESENCES", "GUILD_MESSAGE_REACTIONS", "GUILD_MESSAGE_TYPING"] });
["commands", "aliases"].forEach(x => (bot[x] = new Discord.Collection()));
["command"].forEach(x => require(`./handlers/${x}`)(bot));
require("./handler")(bot);

const discord = require('discord.js');




//message.content.startsWith()
bot.on('message', message => {
  if (message.author.bot) return
  if (message.content.toLowerCase() === "hi") {
message.channel.send('Howdy!')};
});






// now we do events, bot events
//const db = require('quick.db');
//this.mongoose = require('./mongoose.js');
/*bot.on('message',async message => {

  if(message.author.bot) return;

  if (message.channel.type !== 'text') {
    let activ = await db.fetch(`support_${message.author.id}`);
    let guild = bot.guilds.get(message.guild.id)
    let channel,found = true;
    try {
      if (active) bot.channels.get(activ.channelID).guild;
    }



  }






}); */

bot.on("guildMemberAdd", member => {

  const channel = member.guild.channels.find(ch => ch.name === 'gate'); //    const channel = message.guild.channels.get("<guild_id>");
  if(!channel) return;
  const embed = new Discord.RichEmbed()
    .setTitle('A new human has arrived!')
    .setAuthor(member.user.tag, member.user.displayAvatarURL)
    .setThumbnail(member.user.displayAvatarURL)
    .setDescription("User **" + `${member}` + "** has joined the server. Welcome!")
    .setColor('DARKBLUE')
    .setFooter('Joined ' + member.joinedAt);
  channel.send(embed)
});
//WELCOME MESSAGE^^

bot.on("guildMemberRemove", member => {
 // const guild = bot.guilds.get('412307890830049280'); //message.guild.id;
//    const channel = guild.channels.find(ch => ch.name === 'bot-spam');
  //const channel = member.guild.channels.get("412307890830049280");
  const channel = member.guild.channels.find(ch => ch.name === 'gate');
  if(!channel) return;
  const embed = new Discord.RichEmbed()
    .setTitle('A member has departed.')
    .setAuthor(member.user.tag, member.user.displayAvatarURL)
    .setThumbnail(member.user.displayAvatarURL)
    .setDescription("User **" + member.user.username + "** left the server. They won't make it out there, in the wild.")
    .setColor('DARK_RED')
    .setFooter('Left')
    .setTimestamp();
  channel.send(embed)
});
//BAI BAI^


/*bot.on('guildMemberAdd', async member => {
  const channel = member.guild.channels.find(ch => ch.name === 'gate');
  if (!channel) return;

  const canvas = Canvas.createCanvas(700, 250);
  const ctx = canvas.getContext('2d');

  const background = await Canvas.loadImage({files: ["assets/Venox-WELCOME.png"]});
  ctx.drawImage(background, 0, 0, canvas.width, canvas.height);

  ctx.strokeStyle = '#74037b';
  ctx.strokeRect(0, 0, canvas.width, canvas.height);

  // Slightly smaller text placed above the member's display name
  ctx.font = '28px sans-serif';
  ctx.fillStyle = '#ffffff';
  ctx.fillText('Welcome to the server,', canvas.width / 2.5, canvas.height / 3.5);

  // Add an exclamation point here and below
  ctx.font = applyText(canvas, `${member.displayName}!`);
  ctx.fillStyle = '#ffffff';
  ctx.fillText(`${member.displayName}!`, canvas.width / 2.5, canvas.height / 1.8);

  ctx.beginPath();
  ctx.arc(125, 125, 100, 0, Math.PI * 2, true);
  ctx.closePath();
  ctx.clip();

  const avatar = await Canvas.loadImage(member.user.displayAvatarURL({ format: 'jpg' }));
  ctx.drawImage(avatar, 25, 25, 200, 200);

  const attachment = new Discord.MessageAttachment(canvas.toBuffer(), 'welcome-image.png');

  channel.send(`Welcome to the server, ${member}!`, attachment);
});

*/

/*
bot.on("guildMemberAdd", member => {
  console.log(
    "User" + member.user.username + "has joined the server. Welcome!"
  );
  var verify = member.guild.roles.find("name", "Unverified");
  member.addRole('728682867860701308');
});
//GIVES VERIFICATION ROLE^^^
*/
/*
bot.on("message", message => {
  if (message.content.startsWith("v!verify Aj0akCWfebex8n3o")) {
    if (!message.member.roles.has("728682867860701308")) {
      message.channel.send(
        "Either you are already verified or you are not in the correct server to use this command. This command can only be used in the support server of the bot."
      );
    } else {
      message.member.addRole("728682867860701313");
      message.member.removeRole("728682867860701308");
      message.channel.send("You have been verified!");
    }
  }
});
//VERIFICATION^^
*/

bot.on("ready", () => {
  bot.user.setStatus("dnd");

  // this event means, that it will do something when the bot is online
  // i will log in the console that the bot is online when its online
  console.log(
    `${bot.user.username} is ready to watch ${bot.guilds.cache.size} servers!`
  ); /* ${bot.guilds.reduce(
      (prev, val) => val.memberCount + prev,
      0
    )} users and*/ 

    
  // activity

  const status = [
    `${bot.guilds.cache.size} servers and ${bot.users.cache.size} users!`,
    `the prefix: v!`,
    'the support server: dsc.gg/vboy',
    'the Spiderman No Way Home movie 🍿'

  ]; // type here ur status how to do the variable ${bot.guilds.size}
  // it can be users too ok  ok

  setInterval(() => {
    bot.user.setActivity(status.random(), { type: "WATCHING" });
  }, 10000);
});


bot.on("message", message => {
  if (message.author.bot || message.channel.type === "dm") return; // ignore bots and dms
  // yes, in the ready event
  const prefixMention = new RegExp(`^<@!?${bot.user.id}> `);
  const prefix = message.content.match(prefixMention)
    ? message.content.match(prefixMention)[0]
    : "v!"; // <--YOOO PREFIX!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
  const args = message.content
    .slice(prefix.length)
    .trim()
    .split(/ +/g); // this is the arguments, the words after the command, like "v!command a", 'a' is an argument
  const cmd = args.shift().toLowerCase(); // cmd is the actual command name

  if (!message.content.startsWith(prefix)) return; // this means, if the message isnt  a command then ignore

  const commandfile =
    bot.commands.get(cmd) || bot.commands.get(bot.aliases.get(cmd));
  if (commandfile) commandfile.run(bot, message, args);
});



const { REST } = require('@discordjs/rest');
const { Routes } = require('discord-api-types/v9');
const { token } = require('./auth.json');
const fs = require('fs');

const commands = [];
const commandFiles = fs.readdirSync('./commands').filter(file => file.endsWith('.js'));

// Place your client and guild ids here
const clientId = '731650802296422470';
const guildId = '879734848946847774';

for (const file of commandFiles) {
	const command = require(`./commands/${file}`);
	commands.push(command.data.toJSON());
}

const rest = new REST({ version: '9' }).setToken(token);

(async () => {
	try {
		console.log('Started refreshing application (/) commands.');

		await rest.put(
			Routes.applicationGuildCommands(clientId, guildId),
			{ body: commands },
		);

		console.log('Successfully reloaded application (/) commands.');
	} catch (error) {
		console.error(error);
	}
})();




/*
bot.on("message", message => {

  if (message.channel.name === "suggestions") {
    message.react("👍");
    message.react("👎");
    message.react("❔");
  }
});

bot.on("message", message => {

  if (message.channel.name === "vip-suggestions") {
    message.react("👍");
    message.react("👎");
    message.react("❔");
  }
});

bot.on("message", message => {

  if (message.channel.name === "team-suggestions") {
    message.react("👍");
    message.react("👎");
    message.react("❔");
  }
});

bot.on("message", message => {

  if (message.channel.name === "tester-bug-reports") {
    message.react("👍");
    message.react("👎");
    message.react("❔");
  }
});
*/

/*const MongoClient = require('mongodb').MongoClient;
const uri = "mongodb+srv://tekresbot:venoxisdabest@venox-alpha.cj3sm.mongodb.net/tekresbot?retryWrites=true&w=majority";
const client = new MongoClient(uri, { useNewUrlParser: true });
client.connect(err => {
  const collection = client.db("test").collection("devices");
  // perform actions on the collection object
  client.close();
});

*/
//defaut thingie

/*var Long = require("long");

const getDefaultChannel = (guild) => {
  // get "original" default channel
  if(guild.channels.has(guild.id))
    return guild.channels.get(guild.id)

  // Check for a "general" channel, which is often default chat
  const generalChannel = guild.channels.find(channel => channel.name === "general");
  if (generalChannel)
    return generalChannel;
  // Now we get into the heavy stuff: first channel in order where the bot can speak
  // hold on to your hats!

  return guild.channels
   .filter(c => c.type === "text" &&
     c.permissionsFor(guild.client.user).has("SEND_MESSAGES"))
   .sort((a, b) => a.position - b.position ||
     Long.fromString(a.id).sub(Long.fromString(b.id)).toNumber())
   .first();
}
bot.on("guildCreate", guild => {
   getDefaultChannel.send(
    "Hello, if you need help make sure you join our support server! :smile:\n<Insert Link to Support Server>")
      });






/*bot.on("guildCreate", guild => {
  let defaultChannel = "";
  guild.channels.cache.forEach(channel => {
    if (channel.type == "text" && defaultChannel == "") {
      if (channel.permissionsFor(guild.me).has("SEND_MESSAGES")) {
        defaultChannel = channel;
      }
    }
  });
  //defaultChannel will be the channel object that the bot first finds permissions for
  defaultChannel.send(
    "Hello, if you need help make sure you join our support server! :smile:\n<Insert Link to Support Server>"
  );
});

/* bot.on('guildMemberAdd', member => {
    const channel = member.guild.channels.find(ch => ch.name === '⛩gate⛩'); // isnt it "gate" tho lol i was thinkining of my frined's server
    if(!channel) return; //ok done
  channel.send(`Welcome to the server, ${member}, please read <#634802854044893234> and then please verify in <#679081104514351136>. If you need help to verify, please read <#679789670380470294> and if you need furthermore help please contact one of the server admins. For more info about this server, read <#610306634769498152> after verifying.`)
}); */


//bot.login(process.env.TOKEN); // process.env means the .env file where ur token is stored ok


//-----------------------------------------logs-------------------------------------\\
/*

bot.on('message', async (message) => {
 if (message.content.startsWith("v!createlogs")) {
  const logs = message.guild.channels.find(channel => channel.name === "logs");
  if (message.guild.me.hasPermission('MANAGE_CHANNELS') && !logs) {
    message.guild.createChannel('logs', 'text');
    await logs.send(`**Channel Created!**`);
  }
  if (!message.guild.me.hasPermission('MANAGE_CHANNELS') && !logs) {
    console.log('The logs channel does not exist and tried to create the channel but I am lacking permissions')
  }
  //hi
  message.reply(`Either there is already a **logs** channel, or you do not have permission to use this command.`);
}})

//-----------------

bot.on('messageDelete', message => {
const logs = message.guild.channels.find(channel => channel.name === "logs");

const embed = new Discord.RichEmbed()
      .setAuthor(`Deleted by ${message.author.tag}`, message.author.displayAvatarURL)
      .addField('Deleted Message:', message.content)
      .setColor("ORANGE")
      .setFooter(`${message.author.tag} deleted this message in #${message.channel.name}.`);

      logs.send(embed);

})

//('messageUpdate', (oldMessage, newMessage)
/*
bot.on('messageUpdate', (oldMessage, newMessage) => {
const logs = newMessage.guild.channels.find(channel => channel.name === "logs");

const embed = new Discord.RichEmbed()
      .setAuthor(`${newMessage.author.tag} edited a message`, newMessage.author.displayAvatarURL)
      .addField('Old Message:', oldMessage.content)
      .addField('New Message:', newMessage.content)
      .setColor("DARK_ORANGE")
      .setFooter(`${newMessage.author.tag} edited this message in #${newMessage.channel.name}.`);

      logs.send(embed);

}) */

/*
bot.on("channelCreate", channel => {
    const logs = channel.guild.channels.find(channel => channel.name === "logs");


  const embed = new Discord.RichEmbed()
      .setTitle(`New Channel Created`)
      .addField(`Channel Created:`,`${channel.toString()}`)
      .setColor("#00ffd8")
      .setFooter(channel.createdAt);

      logs.send(embed);

  });


bot.on("channelDelete", channel => {
    const logs = channel.guild.channels.find(channel => channel.name === "logs");


  const embed = new Discord.RichEmbed()
      .setTitle(`Channel Deleted`)
      .addField(`Channel Deleted:`,`${channel.toString()}`)
      .setColor("#00ffd8")
      .setTimestamp();

      logs.send(embed);

  });

  */

  //keepAlive()

bot.login(auth.token) 

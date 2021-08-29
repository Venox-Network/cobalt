const owners = ['273538684526264320', '234464614996246529', '242385234992037888'];
const Discord = require('discord.js');
const { RichEmbed } = require('discord.js');
const { Attachment } = require("discord.js");

module.exports = {
  config: {
    name: 'rl',
    description: 'rules',
    usage: 'rules',
    category: 'owner',
    aliases: ['r'],
    accessableby: 'Owners'
  },

    run: async (bot, message, args) => {

     if(!owners.includes(message.author.id)) return message.channel.send('<:redtick:732759534891958322> Only the bot owner can use this command noob.');
   // message.channel.send("Work in progress Wither >:C. Just **__PLEASE__** dont touch this command or i will literally rage.")

    const responses2 = [
   //   "*To invite TekBot to your server, use [this link](https://discordapp.com/oauth2/authorize?client_id=704325829521440778&permissions=8&scope=bot).*", //i need grammerly


    ]

    const responses = [
      "__Please be mindful of the following rules:__\n",
      "`[1]`. **No spamming.** ",
      "`[2]`. **No NSFW content (Inappropriate gifs, images, links, etc.[(This will result in an automatic kick or possibly a ban)]).**",
      "`[3]`. **No cursing (includes acronyms like wtf).**",
      "`[4]`. **Respect all peers and staff.**",
      "`[5]`. **Do not mass ping roles unless you are permitted to.**",
      "`[6]`. **Always read the pins, channel descriptions, and #info to allow less confusion and so staff dont have to repeat themselves.**",
      "`[7]`. **Self promotion or promotion of any kind is not tolerated (includes dms and custom statuses).**",
      "`[8]`. **Anyone under 13 will be banned automatically, for safety purposes.**",
      "`[9]`. **Respect the Discord Terms of Service.**",
      "`[10]`. **Sharing personal content such as passwords and emails is __strictly prohibited__ unless the action is permitted.**",
      "`[11]`. **Scamming or violence is __prohibited__. Please contact a staff member so we may take action as soon as possible (Note: The action must be done in this server).**",
      "`[12]`. **Threatening a raid will result in a kick, participating in a raid will result in a ban.**",
      "`[13]`. **If you agree to follow these rules, you may type __v!verify Aj0akCWfebex8n3o__ in the #verify channel.**",
    ]
/*
**2 warnings**
> 1 day mute

**4 warnings**
> Kick

**5 warnings**
> Permanent mute
> Staff decide ban

*/
    const responses1 = [
      "*Staff are __not__ expected to strictly follow these, they can handle people how they want to/need to*",
      "\n`1 verbal warning` (Your warning will not be logged) = Nothing",
      "`1 warning` = Nothing",
      "`2 warnings` = 1h mute",
      "`3 warnings` = Nothing",
      "`4 warning` = Kick",
      "`5 warnings` = __Permanant mute__ (Staff decide ban)",
      "> After the kick, you must behave for 7 FULL days before all your warnings will be reset",
      "\n*Note: We may change the rules time to time and will notify you in #announcements concerning a change.*",

    ]

    const user = message.mentions.users.first() || message.author;
    const embed = new RichEmbed()
        .setColor("BLUE")
        .setTitle("Server Rules")
       	.setDescription(responses)
   //     .setThumbnail("https://cdn.discordapp.com/attachments/704768364886622238/705948142348271656/Covid-19_Pro_SERVERICON.png")
  //      .setAuthor("Owners: ChrizxzFTW#2879, MrTime#6969, srnyx#0001");

    const embed2 = new RichEmbed()
      .setTitle("Enforcement of rules:")
      .setDescription(responses1)
      .setColor("BLUE")
      .setFooter(`© ${message.guild.me.displayName} | Updated: `) //https:\/\/discord.gg/KB5KxTS
      .setTimestamp();

/*    const embed3 = new RichEmbed()
      .setTitle("Welcome to the server!")
      .setColor("BLUE")
      .setDescription(responses2)
      .setImage({files: ["Venox-WELCOME.png"]});*/
//https://cdn.glitch.com/251f92ee-e939-42a4-af69-704581f1f5da%2FTeky_WELCOME2.png?v=1589893862169
      message.delete(1000);
      await message.channel.send({files: ["assets/Venox-WELCOME.png"]}) //{files: ["assets/Venox-WELCOME.png"]}
      await message.channel.send(embed);
      await message.channel.send(embed2); 
      message.channel.send('https://discord.gg/KB5KxTS');
      //message.channel.send(embed2);
    }

}

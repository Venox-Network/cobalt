const { RichEmbed } = require('discord.js');
const { Attachment } = require("discord.js");

module.exports = {
  config: {
    name: "kill",
    description: "KILL PEOPLE!! Up to 8 kill responses.",
    aliases: ["murder"],
    usage: "<user>",
    category: "fun",
    accessableby: "Member"
  },

  run: async (bot, message, args) => {
    if(!args[0]) return message.channel.send('Provide a user to kill.');
    const user = message.mentions.users.first() || bot.users.get(args[0]);
    if(!user) return message.channel.send('Couldn\'t find that user.');
   // if (user === user.bot.me) return message.channel.send('I won\'t kill my fellow bots!')
    const responses = [
      `${message.author} sliced ${user} in the throat!`,
      `${message.author} shot ${user} with a glock!`,
      `${message.author} poisoned ${user}'s food! ${user} died after eating.`,
      `${message.author} got his gang to beat ${user} to their death!`,
      `${message.author} killed ${user} by showing them a JoJo Siwa video!`,
      `${message.author} hired an assasin to kill ${user}!`,
      `${message.author} shamed ${user} to their death in a Pokemon Battle!`,
      `${message.author} unleashed the reverse card on ${user}! ${user} fell to their death.`
    ];

    const embed = new RichEmbed()
      .setColor('DARK_RED')
      .setAuthor(`${user.username} died`, user.displayAvatarURL)
      .setDescription(responses.random());

    message.channel.send(embed);
  }
};

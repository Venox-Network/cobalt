const Discord = require('discord.js');

module.exports = {
  config: {
    name: 'emojis',
    description: 'A list of the emojis in the server.',
    usage: '',
    category: 'fun',
    accessableby: 'Members',
    aliases: ['emotes']
  },
  run: async (bot, message, args) => {
       const guild = message.guild.name;
    const emotes = message.guild.emojis.map(e => e.toString()) || 'No emojis';
    if (emotes.length === 0) {
      message.channel.send('There are no emojis in this server.');
    } else {
    //message.channel.send(`**__Emojis:__**\n` + emotes.join(' | '));
    const embed = new Discord.RichEmbed()
    .setTitle('Emojis:')
    .setColor('BLUE')
    .setDescription(emotes.join(' | '))
    .setFooter(`Emojis from ` +guild);
    message.channel.send(embed)
  }

  }
}

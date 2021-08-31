const { readdirSync } = require('fs');
const { MessageEmbed } = require('discord.js');

module.exports = {
  config: {
    name: 'help',
    description: 'Get all the commands of the bot.',
    aliases: ['cmds','commands'],
    usage: 'help',
    category: 'main',
    accessableby: ''
  },
  run: async (bot, message, args) => {


    const embed = new MessageEmbed()
		.setColor('0070c0')
		.setAuthor(`${message.guild.me.displayName} Help`, message.guild.iconURL)
		.setThumbnail(bot.user.displayAvatarURL);

    if(!args[0]) {
      const categories = readdirSync('./commands/');

      embed.setDescription(`These are the avaliable commands for ${message.guild.me.displayName}\nThe prefix for this server is: \`v!\``); //Want a further description of the commands? Do \`v!help <spec-ific command>\`*/\n\n
      embed.setFooter(`© ${message.guild.me.displayName} | Total Commands: ${bot.commands.size}`, bot.user.displayAvatarURL);

      categories.forEach(category => {
        const dir = bot.commands.filter(c => c.config.category === category);
        const capitalise = category.slice(0, 1).toUpperCase() + category.slice(1);
        try {
						embed.addField(`> ${capitalise} [${dir.size}]:`, dir.map(c => `\`${c.config.name}\``).join(' \\ '));
					}
					catch(e) {
						console.log(e);
					}
      });

      message.channel.send(embed);
    }

    const data = [];
    var prefix = 'v!';
    if (args[0]) {
      const name = args[0].toLowerCase();
      const cmd = bot.commands.filter(c => c.config.name.toLowerCase() === name);

      data.push(`**Command Name:** ${cmd.config.name}\n`);
      if (cmd.config.aliases) data.push(`**Aliases:** \`${cmd.config.aliases.join('`, `')}\``);
      if (cmd.config.description) data.push(`**Description:** ${cmd.config.description}`);
      if (cmd.config.usage) data.push(`**Usage:** ${prefix}${cmd.config.name} ${cmd.config.usage}`);
      if (cmd.config.category) {
        data.push(`**Category:** ${cmd.config.category || 'No Category'}`);
      } else {
        data.push(`**Category:** No Category`);
      } 
      message.channel.send(data, { split: true });
    }
  }
}
/*
const name = args[0].toLowerCase();
const cmd = bot.commands.filter(c => c.config.name.toLowerCase() === name);

data.push(`**Command Name:** ${cmd.config.name}\n`);
if (cmd.aliases) data.push(`**Aliases:** \`${cmd.aliases.join('`, `')}\``);
if (cmd.description) data.push(`**Description:** ${cmd.description}`);
if (cmd.usage) data.push(`**Usage:** ${prefix}${cmd.name} ${cmd.usage}`);
if (cmd.category) {
  data.push(`**Category:** ${cmd.category || 'No Category'}`);
} else {
  data.push(`**Category:** No Category`);
}
message.channel.send(data, { split: true });
*/

const Discord = require("discord.js");

module.exports = {
  config: {
    name: 'kick',
    description: 'Kick a user - Mod ONLY!',
    aliases: ['kick'],
    usage: '<user>',
    category: 'staff',
    accessableby: 'Staff'
  },
  run: async (bot, message, args) => {
   const channel = message.guild.channels.find(channel => channel.name === "logs");
    const reason = args.slice(1).join(' ') || 'No reason provided';
    //    const channel = message.guild.channels.get("<guild_id>");
    if(!message.member.hasPermission('KICK_MEMBERS')) return message.channel.send('<:redtick:732759534891958322> You don\'t have permission to use that command.');
    if(!message.guild.me.hasPermission('KICK_MEMBERS')) return message.channel.send('<:redtick:732759534891958322> I don\'t have permission to kick.');
    if(!args[0]) return message.channel.send('Provide a user to kick.');
    const user = message.mentions.members.first() || message.guild.members.get(args[0]);
    if(!user) return message.channel.send('<:redtick:732759534891958322> Couldn\'t find that user. Make sure to provide the user\'s id.');
    try {
    await user.kick();
    message.channel.send(`<:greentick:732759504441180260> **Successfully Kicked ${user}**`);

    const embed = new Discord.RichEmbed()
      .setAuthor(`${message.author.tag}`, message.author.displayAvatarURL)
      .setDescription(`${user} has been kicked from the server.`)
      .addField('Reason',reason)
      .setColor("FFFF00")
      .setFooter(`${message.author.tag} peformed the kick command.`);

      channel.send(embed);

    } catch(e) {
      message.channel.send('<:redtick:732759534891958322> Couldn\'t kick that user, check if my roles are higher than the user.');
    }
    },
};

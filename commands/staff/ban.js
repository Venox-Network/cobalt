const Discord = require("discord.js");

module.exports = {
  config: {
    name: 'ban',
    description: 'Ban a user - Mod ONLY!',
    aliases: ['ban'],
    usage: '<user>',
    category: 'staff',
    accessableby: 'Staff'
  },
  run: async (bot, message, args) => {
    const channel = message.guild.channels.find(channel => channel.name === "logs");
    //    const channel = message.guild.channels.get("<guild_id>");
    if(!message.member.hasPermission('BAN_MEMBERS')) return message.channel.send('<:redtick:732759534891958322> You don\'t have permission to use that command.');
    if(!message.guild.me.hasPermission('BAN_MEMBERS')) return message.channel.send('<:redtick:732759534891958322> I don\'t have permission to ban.');
    if(!args[0]) return message.channel.send('Provide a user to ban.');
    const user = message.mentions.members.first() || message.guild.members.get(args[0]);
    const reason = args.slice(1).join(' ') || 'No reason provided';
    if(!user) return message.channel.send('<:redtick:732759534891958322> Couldn\'t find that user. Make sure to provide the user\'s id.');
    try {
    await user.ban();
    message.channel.send(`<:greentick:732759504441180260> **Successfully Banned ${user}**`);

    const embed = new Discord.RichEmbed()
      .setAuthor(`${message.author.tag}`, message.author.displayAvatarURL)
      .setDescription(`🔨${user} has been banned from the server.🔨`)
      .addField('Reason',reason)
      .setColor("8B0000")
      .setFooter(`${message.author.tag} peformed the ban command.`);

      channel.send(embed);

    } catch(e) {
      message.channel.send('<:redtick:732759534891958322> Couldn\'t ban that user, check if my roles are higher than the user.');
    }
    },
};

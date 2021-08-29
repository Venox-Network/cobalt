/*
if(!message.member.hasPermission('BAN_MEMBERS')) return message.channel.send('You don\'t have permission to use that command.');
    if(!message.guild.me.hasPermission('BAN_MEMBERS')) return message.channel.send('I don\'t have permission to purge.');

*/
const Discord = require("discord.js");

module.exports = {
  config: {
    name: "unban",
    description: "Clears the provided amount of messages.",
    usage: "<number>",
    category: "staff",
    accessableby: "Moderators",
    aliases: ["unban"]
  },
  run: async (bot, message, args) => {
    if(!message.member.hasPermission('BAN_MEMBERS')) return message.channel.send('<:redtick:732759534891958322> You don\'t have permission to use that command.');
    if(!message.guild.me.hasPermission('BAN_MEMBERS')) return message.channel.send('<:redtick:732759534891958322> I don\'t have permission to unban.');
    const channel = message.guild.channels.find(channel => channel.name === "logs");
    let User = args[0];

    let Reason = args.slice(1).join(` `);
    if (!User) return message.reply(`<:redtick:732759534891958322> Who are we unbanning?`);
    if(!Reason) Reason = `No reason provided`;

    message.guild.fetchBans().then(bans => {
      if (bans.some(u => User.includes(u.username))) {
        let user = bans.find(user => user.username === User);

        message.guild.unban(user.id, Reason).then(message.channel.send(`<:greentick:732759504441180260> **Successfully Unbanned ${user}**`));
        const embed = new Discord.RichEmbed()
      .setAuthor(`${message.author.tag}`, message.author.displayAvatarURL)
      .setDescription(`${user} has been unbanned from the server.`)
      .addField('Reason',Reason)
      .setColor("00ff2a")
      .setFooter(`${message.author.tag} peformed the unban command.`);

      channel.send(embed);
      } else if (bans.some(u => User.includes(u.id))) {
        message.guild.unban(User, Reason).then(message.channel.send(`<:greentick:732759504441180260> **Successfully Unbanned <@${User}>**`));
        const embed = new Discord.RichEmbed()
      .setAuthor(`${message.author.tag}`, message.author.displayAvatarURL)
      .setDescription(`<@${User}> has been unbanned from the server.`)
      .addField('Reason',Reason)
      .setColor("00ff2a")
      .setFooter(`${message.author.tag} peformed the unban command.`);

      channel.send(embed);
      } else {
        return message.reply(`<:redtick:732759534891958322> Whether the user is not banned, or you have provided an invalid user. Provide the user\'s name or id.`);
      }
    });
  }
};

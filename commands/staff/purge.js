const Discord = require('discord.js');

module.exports = {
    config: {
        name: 'purge',
        description: 'Clears the provided amount of messages.',
        usage: '<number>',
        category: 'staff',
        accessableby: 'Moderators',
        aliases: ['clr', 'clear', 'delete'],
    },
    run: async (bot, message, args) => {
      if(!message.member.hasPermission('MANAGE_MESSAGES')) return message.channel.send('<:redtick:732759534891958322> You don\'t have permission to use that command.');
      const reason = args.slice(1).join(' ') || 'No reason provided';
    const channel = message.guild.channels.find(channel => channel.name === "logs"); //= guild.channels.find(ch => ch.name === 'logs');
        if (!args[0]) return message.channel.send('<:redtick:732759534891958322> Please provide how many messages you want to clear.');
        const amount = parseInt(args[0]) + 1;
        if(isNaN(amount)) return message.channel.send('<:redtick:732759534891958322> You need to provide a valid number.');
        else if(amount <= 1 || amount > 100) return message.channel.send('<:redtick:732759534891958322> You need to input a number between `1-99`');

        message.channel.bulkDelete(amount, true).then( msg => {
            msg.delete(4000);
        }).catch(err => console.error(err));
    var amount2 = amount - 1
   const reply = "<:greentick:732759504441180260> Successfully purged **"+amount2+"** messages"

   message.channel.send(reply).then(msg => msg.delete(3500))

   /* message.channel.send(reply);
    setTimeout(() => {  message.channel.bulkDelete(1, true); }, 3000); */

      const embed = new Discord.RichEmbed()
      .setAuthor(`${message.author.tag}`, message.author.displayAvatarURL)
      .setDescription(`${amount2} messages were purged.`)
      .addField('Reason',reason)
      .setColor("7289db")
      .setFooter(`${message.author.tag} peformed the purge command.`);

      channel.send(embed);
    },
};

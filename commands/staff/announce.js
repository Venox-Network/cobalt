//ChrizxzFTW = 273538684526264320
//MrTime = 234464614996246529
//srnyx = 242385234992037888
//WITHER878 = 416672497195286528

const owners = ['273538684526264320', '234464614996246529', '242385234992037888'];
const Discord = require('discord.js');

module.exports = {
  config: {
    name: 'announce',
    description: 'Announce something to a specific channel.',
    usage: '<#channel> (check | react | reacthere | reacteveryone | here | everyone) <text>',
    category: 'owner',
    aliases: ['say'],
    accessableby: 'Owners'
  },
  run: async (bot, message, args) => {

    // if(!owners.includes(message.author.id)) return message.channel.send('');
    if(!message.member.hasPermission('MANAGE_MESSAGES')) {message.channel.send('<:redtick:732759534891958322> You don\'t have permission to use that command.')
  } else {
      const channel = message.mentions.channels.first();
     if(!channel) return message.channel.send('**How to use:** `<#channel> (check | react | reacthere | reacteveryone | here | everyone) <text>`');
     if(args[1] === 'here') {
      const sayMessage = args.slice(2).join(' ');

      const embed = new Discord.RichEmbed()
      .setTitle('Announcement')
      .setDescription(sayMessage)
      .setColor('F90808')
      .setFooter(`Announcement from ${message.author.tag}`)
      .setTimestamp();

      channel.send('@here', embed);
      message.reply("<:greentick:732759504441180260> Announced!")
    }
    else if(args[1] === 'everyone') {
      const sayMessage = args.slice(2).join(' ');

      const embed = new Discord.RichEmbed()
      .setTitle('Announcement')
      .setDescription(sayMessage)
      .setColor('F90808')
      .setFooter(`Announcement from ${message.author.tag}`)
      .setTimestamp();

      channel.send('@everyone', embed)
      message.reply("<:greentick:732759504441180260> Announced!")
    }
    else if(args[1] === 'reacthere') {
      const sayMessage = args.slice(2).join(' ');

      const embed = new Discord.RichEmbed()
      .setTitle('Vote')
      .setDescription(sayMessage)
      .setColor('088BF9')
      .setFooter(`Announcement from ${message.author.tag}`)
      .setTimestamp();
      const msg = await channel.send('@here', embed);
      await msg.react('⬆️');
      await msg.react('⬇️');
      message.reply("<:greentick:732759504441180260> Announced!")
    }
    else if(args[1] === 'reactrole') {
      const sayMessage = args.slice(2).join(' ');
      const sayRole = args.slice(3).join(' ');
      const embed = new Discord.RichEmbed()
      .setTitle('React for '+sayRole+' role')
      .setDescription(sayMessage)
      .setColor('088BF9')
      .setFooter(`Announcement from ${message.author.tag}`)
      .setTimestamp();
      const msg = await channel.send(embed);
      await msg.react('✅');

      message.reply("<:greentick:732759504441180260> Announced!")
    }
    else if(args[1] === 'check') {
      const sayMessage = args.slice(2).join(' ');

      const embed = new Discord.RichEmbed()
      .setAuthor("Check")
      .setDescription(sayMessage)
      .setColor('b600ff')
      .setFooter(`Announcement from ${message.author.tag}`)
      .setTimestamp();
      const msg = await channel.send(embed);
      await msg.react('🆗');
      message.reply("<:greentick:732759504441180260> Announced!")
    }
    else if(args[1] === 'reacteveryone') {
      const sayMessage = args.slice(2).join(' ');

      const embed = new Discord.RichEmbed()
      .setTitle('Vote')
      .setDescription(sayMessage)
      .setColor('088BF9')
      .setFooter(`Announcement from ${message.author.tag}`)
      .setTimestamp();
      const msg = await channel.send('@everyone', embed);
      await msg.react('⬆️');
      await msg.react('⬇️');
      message.reply("<:greentick:732759504441180260> Announced!")
    }
    else if(args[1] === 'react') {
      const sayMessage = args.slice(2).join(' ');

      const embed = new Discord.RichEmbed()
      .setTitle('Vote')
      .setDescription(sayMessage)
      .setColor('088BF9')
      .setFooter(`Announcement from ${message.author.tag}`)
      .setTimestamp();
      const msg = await channel.send(embed);
      await msg.react('⬆️');
      await msg.react('⬇️');
      message.reply("<:greentick:732759504441180260> Announced!")
    }
    else {
      const sayMessage = args.slice(1).join(' ');

      const embed = new Discord.RichEmbed()
      .setTitle('Announcement')
      .setDescription(sayMessage)
      .setColor('F90808')
      .setFooter(`Announcement from ${message.author.tag}`)
      .setTimestamp();

      channel.send(embed);
      message.reply("<:greentick:732759504441180260> Announced!")
      // done
          }
          }
  }
}

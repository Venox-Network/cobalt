const Discord = require('discord.js');
const { NovelCovid } = require('novelcovid');
const covid = new NovelCovid ();
const moment = require('moment');
require('moment-duration-format');
const fetch = require('node-fetch');

module.exports = {
  config: {
    name: 'covid',
    description: 'Get the statstics of COVID-19.',
    aliases: ['cov'],
    usage: '(country)',
    category: 'main',
    accessableby: ''
  },
  run: async (bot, message, args) => {
    if(!args[0] || args[0].toLowerCase() === 'all') {
        let all = await fetch('https://corona.lmao.ninja/v2/all?yesterday=false');
        all = await all.json();

        const embed = new Discord.RichEmbed()
        .setTitle('COVID-19 Stats')
        .setColor('RANDOM')
        .addField('Cases', all.cases, true)
        .addField('Today cases', all.todayCases, true)
        .addField('Deaths', all.deaths, true)
        .addField('Today deaths', all.todayDeaths, true)
        .addField('Recovered', all.recovered, true)
        .addField('Active cases', all.active, true)
        .addField('Critical', all.critical, true)
        .setFooter(`Last updated: ${moment.utc(all.updated).format('L, h:mm A UTC')}`);

        message.channel.send(embed);
      }
      else if(args[0].toLowerCase() === 'list') {
        let list = await fetch('https://corona.lmao.ninja/v2/countries');
        list = await list.json();
        const embed = new Discord.MessageEmbed()
        .setTitle('Country list')
        .setColor('RANDOM')
        .setDescription(list.map(c => `\`${c.country}\``).join(' - ').slice(1264, 2529));

        const msg = await message.channel.send(embed);
        await msg.react('◀️');
        await msg.react('▶️');
        const forward = msg.createReactionCollector((reaction, user) => reaction.emoji.name === '▶️' && user.id === message.author.id, { time: 60000 });
        const backward = msg.createReactionCollector((reaction, user) => reaction.emoji.name === '◀️' && user.id === message.author.id, { time: 60000 });

        forward.on('collect', r => {

          r.users.remove(message.author);

          const embed2 = new Discord.MessageEmbed()
          .setTitle('Country list')
          .setColor('RANDOM')
          .setDescription(list.map(c => `\`${c.country}\``).join(' - ').slice(0, 1264));

          msg.edit(embed2);
        });

        backward.on('collect', r => {
          r.users.remove(message.author);
          msg.edit(embed);
        });
      }
      else {
        const country = args.join(' ').toLowerCase();
        let res = await fetch('https://corona.lmao.ninja/v2/countries');
        res = await res.json();
        const result = await res.find(c => c.country.toLowerCase().includes(country));
        if(!result) return message.channel.send('That country doesn\'t exist, or the virus is not there yet.');

        const embed = new Discord.RichEmbed()
        .setTitle(result.country)
        .setColor('RANDOM')
        .setThumbnail(result.countryInfo.flag)
        .addField('Cases', result.cases, true)
        .addField('Today cases', result.todayCases, true)
        .addField('Deaths', result.deaths, true)
        .addField('Today deaths', result.todayDeaths, true)
        .addField('Recovered', result.recovered, true)
        .addField('Active cases', result.active, true)
        .addField('Critical', result.critical, true)
        .setFooter(`Last updated: ${moment.utc(country.updated).format('L, h:mm A UTC')}`);

        message.channel.send(embed);
      }
  }
}

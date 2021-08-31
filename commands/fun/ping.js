module.exports = {
  config: {
    name: 'ping',
    description: 'Get the bot and API latency.',
    aliases: ['p'],
    usage: 'v!\`p\`',
    category: 'fun',
    accessableby: 'anyone'
  },
  run: async (bot, message, args) => {
    const msg = await message.channel.send('Pinging...');

    const ping = msg.createdTimestamp - message.createdTimestamp;

    msg.edit(`PingPong sucks but ok :p\n**Bot latency is ${ping}ms, API latency is ${Math.round(bot.ping)}ms.**`);

  }
}

const interactions = require("discord-slash-commands-client");

const bot = new interactions.Client(
  auth.token,
  "731650802296422470"
);


bot.createCommand({
  name: "test",
  description: "description for this command",
})

message.channel.send('srnyx is cringe eggsdee')

.catch(console.error)
.then(console.log);
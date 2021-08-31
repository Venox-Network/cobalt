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

run: async (bot, message, args) => {
const interactions = require("discord-slash-commands-client");

const client = new interactions.Client(
  "NzMxNjUwODAyMjk2NDIyNDcw.XwpI6A.qz2a8Gw1ubU5ZVy1sSnGMk081Ys",
  "731650802296422470"
);


client.createCommand({
  name: "test",
  description: "description for this command",
})

message.channel.send('srnyx is cringe eggsdee')

.catch(console.error)
.then(console.log);
}
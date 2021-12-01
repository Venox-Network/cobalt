const { inspect } = require("util");
const owners = [
  "273538684526264320",
  "242385234992037888",
];

module.exports = {
    name: "eval",
    aliases: ["e"],
    description: "eval command (owner only)",
    /**
     *
     * @param {Client} client
     * @param {Message} message
     * @param {String[]} args
     */
  run: async (client, message, args) => {
    try {
      if (!owners.includes(message.author.id))
        return message.channel.send(
          "<:redtick:732759534891958322> Only the bot owner can use this command."
        );
      const toEval = args.join(" ");
      const hrStart = process.hrtime();
      const evaluated = inspect(eval(toEval), { depth: 0 });
      const hrDiff = process.hrtime(hrStart);

      if (evaluated.length >= 2000)
        return message.channel.send(
          "<:redtick:732759534891958322> Evaluation is too long!"
        );

      message.channel.send(
        `Executed in ${hrDiff[0] > 0 ? `${hrDiff[0]}s ` : ""}${
          hrDiff[1] / 1000000
        }ms. \`\`\`js\n${evaluated}\`\`\``
      );
    } catch (e) {
      console.log(e);
      message.channel.send(
        `<:redtick:732759534891958322> Error while evaluating: \`${e}\``
      );
    }
  },
};

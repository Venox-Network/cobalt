const { inspect } = require("util");
const owners = [
  "273538684526264320",
  "242385234992037888",
];

module.exports = {
    name: "eval",
    description: "eval command (owner only)",
    options: [
        {
            name: "code",
            description: "code for eval command",
            type: "STRING",
            required: true,
        }
    ],
    /**
     *
     * @param {Client} client
     * @param {CommandInteraction} interaction
     * @param {String[]} args
     */
  run: async (client, interaction, args) => {
    try {
      if (!owners.includes(interaction.user.id))
        return interaction.followUp(
          "Only the bot owners can use this command."
        );
      const toEval = interaction.options.get("code").value;
      const hrStart = process.hrtime();
      const evaluated = inspect(eval(toEval), { depth: 0 });
      const hrDiff = process.hrtime(hrStart);

      if (evaluated.length >= 2000)
        return interaction.followUp(
          "Evaluation is too long!"
        );

      interaction.followUp(
        `Executed in ${hrDiff[0] > 0 ? `${hrDiff[0]}s ` : ""}${
          hrDiff[1] / 1000000
        }ms. \`\`\`js\n${evaluated}\`\`\``
      );
    } catch (e) {
      console.log(e);
      interaction.followUp(
        `Error while evaluating: \`${e}\``
      );
    }
  },
};

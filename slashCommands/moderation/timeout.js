//const { Command } = require("reconlx");
const ms = require("ms");
module.exports = {
  name: "timeout",
  description: "timeout a member",
  userPermissions: "MODERATE_MEMBERS",
  botPermissions: "MODERATE_MEMBERS",
  options: [
    {
      name: "user",
      description: "member to perform the timeout",
      type: "USER",
      required: true,
    },
    {
      name: "length",
      description: "Example: 1 minute",
      type: "STRING",
      required: true,
    },
    {
      name: "reason",
      description: "reason for the timeout",
      type: "STRING",
      required: true,
    },
  ],
  run: async (client, interaction) => {
    
    const target = interaction.options.getMember("user");
    const length = interaction.options.getString("length");
    const reason =
      interaction.options.getString("reason") || "No reason provided";
    const member = interaction.guild.members.cache.get(target.id);
    const timeInMs = ms(length);

    if (
      target.roles.highest.position >= interaction.member.roles.highest.position
    )
      return interaction.followUp({
        content: "❌ | This user's role is higher than yours",
      });

      

    try {
      if (!timeInMs)
        return interaction.followUp("❌ | Please specify a valid time!");

      member.timeout(timeInMs, reason);
      interaction.followUp({
        content: `✅ | ${target} has been muted for ${length}.\nReason: ${reason}`,
        ephemeral: true,
      });

      target.send(`You've been timed out in **${interaction.guild.name}** for **${length}**\nReason: ${reason}`);

    } catch(error) {
      console.log(error);
      interaction.followUp({
        content:
        "❌ | There was an error trying to execute that command: " + `\`${error.message}\``,
      });
    }
  },
};

//<:redtick:732759534891958322>

//const { Command } = require("reconlx");
const ms = require("ms");
module.exports = {
  name: "timeout",
  description: "timeout a member",
  options: [
    {
      name: "user",
      description: "member to peform the timeout",
      type: "USER",
      required: true,
    },
    {
      name: "length",
      description: "length of the timeout",
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
    const user = interaction.options.getUser("user");
    const length = interaction.options.getString("length");
    const reason = interaction.options.getString("reason");
    const member = interaction.guild.members.cache.get(user.id);

    if (!interaction.member.permissions.has("MODERATE_MEMBERS"))
      return interaction.followUp({
        content: " You don't have permission to use that command!",
      });
    if (!interaction.guild.me.permissions.has("MODERATE_MEMBERS"))
      return interaction.followUp({
        content: "I don't have permission to timeout members!",
      });

    try {
      const timeInMs = ms(length);
      if (!timeInMs)
        return interaction.followUp("Please specify a valid time!");

      member.timeout(timeInMs, reason);
      interaction.followUp(
        `${user} has been muted for ${length}.\nReason: ${reason}`
      );
    } catch (e) {
      interaction.followUp({
        content:
          "Couldn't kick that user, check if my roles/permissions are higher than the user.",
      });
    }
  },
};

//<:redtick:732759534891958322>

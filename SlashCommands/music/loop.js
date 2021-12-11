const player = require("../../client/player");

module.exports = {
  name: "loop",
  description: "loops the queue",
  run: async (client, interaction, args) => {
    const queue = player.getQueue(interaction.guildId);
    if (!queue?.playing)
      return interaction.followUp({
        content: "No music is currently being played :(",
      });


    interaction.followUp({ content: "In developement :O" });
  },
};

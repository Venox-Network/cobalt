const player = require("../../client/player");

module.exports = {
  name: "back",
  description: "plays previous track",
  run: async (client, interaction, args) => {
    const queue = player.getQueue(interaction.guildId);
    if (!queue?.playing)
      return interaction.followUp({
        content: "No music is currently being played :(",
      });

    await queue.back();

    interaction.followUp({ content: "Playing previous song 🔙" });
  },
};

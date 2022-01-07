const player = require("../../client/player");

module.exports = {
  name: "shuffle",
  description: "shuffles the queue",
  run: async (client, interaction, args) => {
    const queue = player.getQueue(interaction.guildId);
    if (!queue?.playing)
      return interaction.followUp({
        content: "No music is currently being played :(",
      });

    await queue.shuffle();
    queue.skip();

    interaction.followUp({ content: "Shuffled 🔀" });
  },
};

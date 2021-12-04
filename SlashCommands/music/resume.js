const player = require("../../client/player");

module.exports = {
  name: "resume",
  aliases: ["unpause"],
  description: "resume the current song",
  run: async (client, interaction) => {
    const queue = player.getQueue(interaction.guildId);

    queue.setPaused(false);

    return interaction.followUp({ content: "**Resumed** :play_pause:" });
  },
};

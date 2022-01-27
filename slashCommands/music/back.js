const player = require("../../client/player");

module.exports = {
  name: "back",
  description: "plays previous track",
  run: async (client, interaction, args) => {
    if (!interaction.member.voice.channel)
      return interaction.followUp({
        content: "❌ | Join a voice channel first",
      });

    if (
      interaction.guild.me.voice.channelId &&
      interaction.member.voice.channelId !==
        interaction.guild.me.voice.channelId
    ) {
      interaction.followUp({
        content: "❌ | You are not in my voice channel",
        ephemeral: true,
      });
    }

    const queue = player.getQueue(interaction.guildId);
    if (!queue?.playing)
      return interaction.followUp({
        content: "❌ | No music is currently being played",
      });

    await queue.back();

    interaction.followUp({ content: `🔙 | Playing **${queue.current.title}** ` });
  },
  catch(error) {
    console.log(error);
    interaction.followUp({
      content:
        "❌ | There was an error trying to execute that command: " + `\`${error.message}\``,
    });
  },
};

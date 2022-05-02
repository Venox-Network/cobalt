const player = require("../../client/player");

module.exports = {
  name: "pause",
  description: "pause the current song",
  run: async (client, interaction) => {
    if (!interaction.member.voice.channel)
      return interaction.followUp({
        content: "❌ | Join a voice channel first",
      });

    if (interaction.guild.me.voice.channelId && interaction.member.voice.channelId !== interaction.guild.me.voice.channelId) {
      await interaction.followUp({
        content: "❌ | You are not in my voice channel",
        ephemeral: true,
      });
    }
    
    const queue = player.getQueue(interaction.guildId);
    if (!queue?.playing)
      return interaction.followUp({
        content: "❌ | No music is currently being played",
      });

    queue.setPaused(true);

    return interaction.followUp({ content: ":pause_button: | Paused" });
  },
  catch(error) {
    console.log(error);
    //FIXME Unresolved variable or type interaction
    //FIXME Promise returned from followUp is ignored
    interaction.followUp({
      content:
        "❌ | There was an error trying to execute that command: " + `\`${error.message}\``,
    });
  },
};

const player = require("../../client/player");

module.exports = {
  name: "volume",
  description: "change or check the volume of the current song",
  options: [
    {
      name: "amount",
      description: "percentage to change the volume to",
      type: "INTEGER",
      required: false,
    },
  ],
  run: async (client, interaction) => {
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


    const volumePercentage = interaction.options.getInteger("amount");
    const queue = player.getQueue(interaction.guildId);
    if (!queue?.playing)
      return interaction.followUp({
        content: "❌ | No music is currently being played",
      });

    const volumeStatus = queue.volume <= 10 ? "🔉" : volumePercentage <= 50 ? "🔉" : "🔊" ;

    if (!volumePercentage)
      return interaction.followUp({
        content: `${volumeStatus} The current volume is **${queue.volume}%**`,
      });

    if (volumePercentage < 0 || volumePercentage > 100)
      return interaction.followUp({
        content: "❌ | The volume must be betweeen 1 and 100",
      });

    queue.setVolume(volumePercentage);

    const emoji = volumePercentage <= 10 ? "🔉" : volumePercentage <= 50 ? "🔉" : "🔊" ;

    return interaction.followUp({
      content: `${emoji} | Volume has been set to **${volumePercentage}%**`,
    });
  },
  catch(error) {
    console.log(error);
    interaction.followUp({
      content:
        "❌ | There was an error trying to execute that command: " + `\`${error.message}\``,
    });
  },
};

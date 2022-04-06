const player = require("../../client/player");

module.exports = {
  name: "nowplaying",
  description: "shows information about the current song",
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

    const progress = queue.createProgressBar();
    const perc = queue.getPlayerTimestamp();

    return interaction.followUp({
      embeds: [
        {
          title: "Now Playing",
          description: `🎶 | [**${queue.current.title}**](${queue.current.url}) (\`${perc.progress}%\`)`,
          fields: [
            {
              name: "\u200b",
              value: progress,
            },
          ],
          color: "0070c0",
          footer: {
            text: `Queued by ${queue.current.requestedBy.tag} | Venox Music`,
            icon_url: client.user.displayAvatarURL()
          },
        },
      ],
    });
  },
  catch(error) {
    console.log(error);
    //FIXME interaction is undefined
    interaction.followUp({
      content:
          "❌ | There was an error trying to execute that command: " + `\`${error.message}\``,
    });
  },
};

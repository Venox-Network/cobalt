const player = require("../../client/player");

module.exports = {
  name: "queue",
  description: "display the song queue",
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
        content: "❌ | No songs are currently playing",
      });

    const currentTrack = queue.current;
    const tracks = queue.tracks.slice(0, 15).map((m, i) => {
      return `${i + 1}. [**${m.title}**](${m.url}) - ${m.requestedBy.tag}`;
    });

    return interaction.followUp({
      embeds: [
        {
          title: "Song Queue",
          description: `${tracks.join("\n")}${
            queue.tracks.length > tracks.length
              ? `\n...${
                  queue.tracks.length - tracks.length === 1
                    ? `${queue.tracks.length - tracks.length} more track`
                    : `${queue.tracks.length - tracks.length} more tracks`
                }`
              : ""
          }`,
          color: "0070c0",
          fields: [
            {
              name: "Now Playing",
              value: `🎶 | [**${currentTrack.title}**](${currentTrack.url}) - ${currentTrack.requestedBy.tag}`,
            },
          ],
          footer: {
            text: "Venox Music",
            icon_url: client.user.displayAvatarURL()
          },
        },
      ],
    });
  },
  catch(error) {
    console.log(error);
    //FIXME interaction is undefined
    //FIXME Promise returned from followUp is ignored
    interaction.followUp({
      content:
        "❌ | There was an error trying to execute that command: " + `\`${error.message}\``,
    });
  },
};

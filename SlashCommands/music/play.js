const { QueryType } = require("discord-player");
const player = require("../../client/player");

module.exports = {
  name: "play",
  aliases: ["p"],
  description: "play a song",
  options: [
    {
      name: "songtitle",
      description: "title of the song",
      type: "STRING",
      required: true,
    },
  ],
  run: async (client, interaction) => {
    const songTitle = interaction.options.getString("songtitle");

    if (!interaction.member.voice.channel)
      return interaction.followUp({
        content: "Join a voice channel first!",
      });

    const searchResult = await player.search(songTitle, {
      requestedBy: interaction.user,
      searchEngine: QueryType.AUTO,
    });

    const queue = await player.createQueue(interaction.guild, {
      metadata: interaction.channel,
    });

    if (!queue.connection)
      await queue.connect(interaction.member.voice.channel);

    interaction.followUp({
      content: `> Playing **${songTitle}** :musical_note:`,
    });

    searchResult.playlist
      ? queue.addTracks(searchResult.tracks)
      : queue.addTrack(searchResult.tracks[0]);

    if (!queue.playing) await queue.play();
  },
};

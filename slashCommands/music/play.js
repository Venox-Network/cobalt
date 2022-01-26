const { QueryType } = require("discord-player");
const player = require("../../client/player");
const { MessageEmbed } = require("discord.js");
const { getVoiceConnection } = require('@discordjs/voice');

module.exports = {
  name: "play",
  description: "play a song",
  userPermissions: "CONNECT",
  botPermissions: "",
  options: [
    {
      name: "song",
      description: "title of the song",
      type: "STRING",
      required: true,
    },
  ],
  run: async (client, interaction) => {
    const query = interaction.options.getString("song");

    if (!interaction.member.voice.channel)
      return interaction.followUp({
        content: "Join a voice channel first!",
      });

    const searchResult = await player.search(query, {
      requestedBy: interaction.user,
      searchEngine: QueryType.AUTO,
    });//.then(x => x.tracks[0]);
    if (!searchResult) return;

    const queue = await player.createQueue(interaction.guild, {
      metadata: interaction.channel,
    });

    if (!queue.connection)
      await queue.connect(interaction.member.voice.channel);
      
      const searchResults = await player.search(query, {
        requestedBy: interaction.user,
        searchEngine: QueryType.AUTO,
      }).then(x => x.tracks[0]);
      if (!searchResults) return await interaction.followUp({ content: `Sorry, **${query}** not found :(` });

      function capitalizeFirstLetter(string) {
        return string.charAt(0).toUpperCase() + string.slice(1);
      }

    interaction.followUp({
      content: `Playing **${searchResults.title}** :musical_note:`,
    });


    searchResult.playlist
      ? queue.addTracks(searchResult.tracks)
      : queue.addTrack(searchResult.tracks[0]);

    if (!queue.playing) await queue.play();

  },
};

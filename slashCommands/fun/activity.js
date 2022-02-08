const discordTogether = require("../../client/discordTogether");
const {
  CommandInteraction,
  Client,
  Message,
  MessageActionRow,
  MessageButton,
} = require("discord.js");

module.exports = {
  name: "activity",
  description: "play a select activity",
  options: [
    {
      name: "activity",
      description: "activity you want to play",
      type: "STRING",
      required: true,
      choices: [
        {
          name: "Watch Together",
          value: "watchtogether",
        },
        {
          name: "Doodle Crew",
          value: "doodlecrew",
        },
        {
          name: "Fishington",
          value: "fishington",
        },
        {
          name: "Poker",
          value: "poker",
        },
        {
          name: "Chess in the Park",
          value: "chess",
        },
        {
          name: "Checkers in the Park",
          value: "checkers",
        },
        {
          name: "Betrayal.io",
          value: "betrayal",
        },
        {
          name: "Letter League",
          value: "letterleague",
        },
        {
          name: "Words Snack",
          value: "wordsnack",
        },
        {
          name: "SpellCast",
          value: "spellcast",
        },
        {
          name: "Awkword",
          value: "awkword",
        },
        {
          name: "Puttparty",
          value: "puttparty",
        },
        {
          name: "Sketch Heads (Coming Soon)",
          value: "sketchheads",
        },
        {
          name: "Ocho",
          value: "ocho",
        }
      ],
    },
    {
      name: "channel",
      description: "channel to start the activity",
      type: "CHANNEL",
      channelTypes: ["GUILD_VOICE"],
      required: true,
    },
  ],
  /**
   *
   * @param {Client} client
   * @param {CommandInteraction} interaction
   * @param {String[]} args
   *
   */
  run: async (client, interaction, args) => {
    //const [channelID] = args[1];
    const channelID = interaction.options.getChannel("channel").id;
    const channel = interaction.options.getChannel("channel");
    //const channel = interaction.guild.channels.cache.get(channelID);
    const name = interaction.options.get("activity").name;
    /*
    if (channel.type !== "GUILD_VOICE")
      return interaction.followUp({
        content: "Please choose a voice channel!",
      });*/

    const value = interaction.options.get("activity").value;

    if (value === "watchtogether") {
      discordTogether
        .createTogetherCode(channelID, "youtube")
        .then((x) =>
          interaction.followUp(
            `[Click to start ${name} in ${channel.name}!](${x.code})`
          )
        );
    } else if (value === "doodlecrew") {
      discordTogether
        .createTogetherCode(channelID, "doodlecrew")
        .then((x) =>
          interaction.followUp(
            `[Click to start ${name} in ${channel.name}!](${x.code})`
          )
        );
    } else if (value === "fishington") {
      discordTogether
        .createTogetherCode(channelID, "fishing")
        .then((x) =>
          interaction.followUp(
            `[Click to start ${name} in ${channel.name}!](${x.code})`
          )
        );
    } else if (value === "poker") {
      discordTogether
        .createTogetherCode(channelID, "poker")
        .then((x) =>
          interaction.followUp(
            `[Click to start ${name} in ${channel.name}!](${x.code})`
          )
        );
    } else if (value === "chess") {
      discordTogether
        .createTogetherCode(channelID, "chess")
        .then((x) =>
          interaction.followUp(
            `[Click to start ${name} in ${channel.name}!](${x.code})`
          )
        );
    } else if (value === "checkers") {
      discordTogether
        .createTogetherCode(channelID, "checkers")
        .then((x) =>
          interaction.followUp(
            `[Click to start ${name} in ${channel.name}!](${x.code})`
          )
        );
    } else if (value === "betrayal") {
      discordTogether
        .createTogetherCode(channelID, "betrayal")
        .then((x) =>
          interaction.followUp(
            `[Click to start ${name} in ${channel.name}!](${x.code})`
          )
        );
    } else if (value === "letterleague") {
      discordTogether
        .createTogetherCode(channelID, "lettertile")
        .then((x) =>
          interaction.followUp(
            `[Click to start ${name} in ${channel.name}!](${x.code})`
          )
        );
    } else if (value === "wordsnack") {
      discordTogether
        .createTogetherCode(channelID, "wordsnack")
        .then((x) =>
          interaction.followUp(
            `[Click to start ${name} in ${channel.name}!](${x.code})`
          )
        );
    } else if (value === "spellcast") {
      discordTogether
        .createTogetherCode(channelID, "spellcast")
        .then((x) =>
          interaction.followUp(
            `[Click to start ${name} in ${channel.name}!](${x.code})`
          )
        );
    } else if (value === "awkword") {
      discordTogether
        .createTogetherCode(channelID, "awkword")
        .then((x) =>
          interaction.followUp(
            `[Click to start ${name} in ${channel.name}!](${x.code})`
          )
        );
    } else if (value === "puttparty") {
      discordTogether
        .createTogetherCode(channelID, "puttparty")
        .then((x) =>
          interaction.followUp(
            `[Click to start ${name} in ${channel.name}!](${x.code})`
          )
        );
    } else if (value === "sketchheads") {
      discordTogether
        .createTogetherCode(channelID, "sketchheads")
        .then((x) =>
          interaction.followUp(
            `[Click to start ${name} in ${channel.name}!](${x.code})`
          )
        )
      } else if (value === "ocho") {
        discordTogether
          .createTogetherCode(channelID, "ocho")
          .then((x) =>
            interaction.followUp(
              `[Click to start ${name} in ${channel.name}!](${x.code})`
            )
          )
      };
    /*else if (value === "sketchheads") {
      interaction.followUp(`**Coming soon!**`);
    }*/
    
  },
};

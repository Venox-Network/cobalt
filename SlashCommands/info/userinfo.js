const { Client, CommandInteraction } = require("discord.js");

module.exports = {
    name: "userinfo",
    description: "returns user information",
    type: 'CHAT_INPUT',
    /**
     *
     * @param {Client} client
     * @param {CommandInteraction} interaction
     * @param {String[]} args
     */
    run: async (client, interaction, args) => {
        interaction.reply({ 
            content: "Work in progress :(", 
            ephemeral: true 
        
        
        });
    },
};

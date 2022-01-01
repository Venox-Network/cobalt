const { Command } = require("reconlx"); 
const ms = require("ms");
module.exports = new Command({
    name: 'timemout',
    description: 'timemout a member',
    options: [
        {
            name: 'user',
            description: 'member to peform the timemout',
            type: 'USER',
            required: true
        },
        {
            name: 'length',
            description: 'length of the timemout',
            type: 'STRING',
            required: true
        },
        {
            name: 'reason',
            description: 'reason for the timemout',
            type: 'STRING',
            required: true
        }
    ],
    run: async({interaction}) => {
        const user = interaction.options.getUser('user')
        const length = interaction.options.getString('length')
        const reason = interaction.options.getString('reason')
        const members = interaction.guild.members.cache.get(user.id)
        
        const timeInMs = ms(length);
        if (!timeInMs)
            return interaction.followUp('Please specify a valid time!');

        member.timeout(timeInMs, reason);
        interaction.followUp(`${user} has been muted for ${length}.\nReason: ${reason}`);

    }
});
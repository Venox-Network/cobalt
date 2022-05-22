import discord
from config import Config
from discord_bot import Bot
from cogs import (music_commands, 
        time_jobs, 
        staff_commands, 
        info_commands, 
        fun_commands, 
        super_commands, 
        report_commands, 
        auto_thread, 
        slowmode_commands,
        nick_name_commands,
        sticky_message
    )

def main():
    main_config:Config = Config.get_conf_from_file()
    
    intents = discord.Intents.default()
    intents.members = True
    intents.message_content = True

    main_bot: Bot = Bot(conf=main_config, intents=intents)

    main_bot.add_cogs(
        time_jobs.cog_creator,
        staff_commands.cog_creator,
        info_commands.cog_creator,
        fun_commands.cog_creator,
        super_commands.cog_creator,
        report_commands.cog_creator,
        auto_thread.cog_creator,
        #music_commands.cog_creator,
        slowmode_commands.cog_creator,
        nick_name_commands.cog_creator,
        sticky_message.cog_creator
    )

    main_bot.run(main_config.BOT_TOKEN)
    

if __name__ == '__main__':
    main()
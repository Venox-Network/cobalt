import discord
from config import Config
from discord_bot import Bot
from cogs.commands.fun import music_commands
from cogs import time_jobs
from cogs.commands.fun import music_commands
from cogs.commands.moderation import cmd_ban, cmd_kick, cmd_mute, cmd_purge, cmd_unmute, cmds_report, cmd_slowmode, cmds_warns, cmd_super_ban, cmd_super_kick, cmd_super_unban, cmd_sticky_message, cmd_eval
from cogs.commands.utility import cmd_leave_server, cmd_react, cmd_servers, cmd_support,\
    cmds_nickname, cmd_auto_thread, cmds_global_chat, cmd_help

def main():
    main_config = Config.get_conf_from_file()

    intents = discord.Intents.default()
    intents.members = True
    intents.message_content = True
    global main_bot
    main_bot = Bot(conf=main_config, intents=intents)

    main_bot.add_cogs(
        # misc commands
        music_commands.cog_creator,
        # utility commands
        cmd_leave_server.cog_creator,
        cmd_servers.cog_creator,
        cmd_support.cog_creator,
        cmd_auto_thread.cog_creator,
        cmds_nickname.cog_creator,
        cmd_react.cog_creator,
        cmds_global_chat.cog_creator,
        cmd_help.cog_creator,
        # moderation commands
        cmd_eval.cog_creator,
        cmd_super_ban.cog_creator,
        cmd_super_unban.cog_creator,
        cmd_super_kick.cog_creator,
        cmds_report.cog_creator,
        cmd_slowmode.cog_creator,
        cmd_sticky_message.cog_creator,
        cmd_ban.cog_creator,
        cmds_warns.cog_creator,
        cmd_kick.cog_creator,
        cmd_mute.cog_creator,
        cmd_purge.cog_creator,
        cmd_unmute.cog_creator,
        # time jobs
        time_jobs.cog_creator,
    )
    main_bot.run(main_config.BOT_TOKEN)


if __name__ == '__main__':
    main()

from typing import List

import orjson
import motor.motor_asyncio


class Config:
    def __init__(self, token: str, cluster: str, global_report_channel: int, global_log_channel: int, owners: List[int], wavelink_host: str, wavelink_pass: str, wavelink_port: str, debug_servers: List[int]) -> None:
        self.BOT_TOKEN: str = token
        MOTOR_CLUSTER = motor.motor_asyncio.AsyncIOMotorClient(cluster)
        self.GLOBAL_REPORT_CHANNEL: int = int(global_report_channel)
        self.GLOBAL_LOG_CHANNEL: int = int(global_log_channel)
        self.OWNERS: List[int] = owners

        self.DATABASE = MOTOR_CLUSTER["cobalt"]
        self.wavelink_host = wavelink_host
        self.wavelink_pass = wavelink_pass
        self.wavelink_port = int(wavelink_port)
        self.debug_servers = debug_servers

    @classmethod
    def get_conf_from_file(cls, path:str="config.json") -> 'Config':
        try:
            with open(path, "rb") as file:
                config = orjson.loads(file.read())
        except FileNotFoundError:
            print(f"Config file: '{path}' not found! Creating new config file in '{path}'")
            print("Generating config.json...")
            with open(path, "w") as f:
                f.write("""{
    "bot-token": "",
    "mongodb-con": "",
    "global-report-channel-id": "",
    "global-log-channel-id": "",
    "wavelink-host": "",
    "wavelink-pass": "",
    "wavelink-port": "",
    "owners":[
    ],
    "debug_servers":[
    ]
}""")
            exit()
        except orjson.JSONDecodeError:
            print("Config could not be decoded. Please check if the json syntax is valid.")
            exit()

        if any(bool(val) == False for key, val in config.items() if key != "debug_servers"):
            print("Config file is missing some values. Please fill in the missing values.")
            exit()

        return cls(
            config["bot-token"], 
            config["mongodb-con"], 
            config["global-report-channel-id"], 
            config["global-log-channel-id"], 
            config["owners"],
            config["wavelink-host"],
            config["wavelink-pass"],
            config["wavelink-port"],
            config.get("debug_servers", None)
        )

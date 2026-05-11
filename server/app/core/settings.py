from pathlib import Path

from pydantic import Field
from pydantic_settings import BaseSettings, SettingsConfigDict

BASE_DIR = Path(__file__).resolve().parents[2]


class Settings(BaseSettings):
    model_config = SettingsConfigDict(
        env_file=str(BASE_DIR / ".env"),
        env_file_encoding="utf-8",
        extra="ignore",
    )

    db_driver: str = Field(default="mysql", alias="DB_DRIVER")

    app_host: str = Field(default="127.0.0.1", alias="APP_HOST")
    app_port: int = Field(default=3000, alias="APP_PORT")
    app_reload: bool = Field(default=True, alias="APP_RELOAD")
    ffmpeg_path: str = Field(default="ffmpeg", alias="FFMPEG_PATH")
    storage_root: str = Field(default=str(BASE_DIR / "storage"), alias="STORAGE_ROOT")

    mysql_host: str = Field(default="127.0.0.1", alias="MYSQL_HOST")
    mysql_port: int = Field(default=3306, alias="MYSQL_PORT")
    mysql_user: str = Field(default="root", alias="MYSQL_USER")
    mysql_password: str = Field(default="change_me", alias="MYSQL_PASSWORD")
    mysql_database: str = Field(default="yingrensheng", alias="MYSQL_DATABASE")
    sqlite_path: str = Field(default=str(BASE_DIR / "yingrensheng.db"), alias="SQLITE_PATH")
    vod_access_key_id: str = Field(default="", alias="VOD_ACCESS_KEY_ID")
    vod_secret_access_key: str = Field(default="", alias="VOD_SECRET_ACCESS_KEY")
    vod_space_name: str = Field(default="", alias="VOD_SPACE_NAME")
    vod_region: str = Field(default="cn-north-1", alias="VOD_REGION")
    ark_text_base_url: str = Field(default="https://ark.cn-beijing.volces.com/api/v3", alias="ARK_TEXT_BASE_URL")
    ark_text_api_key: str = Field(default="", alias="ARK_TEXT_API_KEY")
    ark_text_model: str = Field(default="doubao-seed-1-8-251228", alias="ARK_TEXT_MODEL")
    ark_i2v_base_url: str = Field(default="https://ark.cn-beijing.volces.com/api/v3", alias="ARK_I2V_BASE_URL")
    ark_i2v_api_key: str = Field(default="", alias="ARK_I2V_API_KEY")
    ark_i2v_model: str = Field(default="doubao-seedance-1-5-pro-251215", alias="ARK_I2V_MODEL")

    @property
    def sqlalchemy_database_url(self) -> str:
        if self.db_driver.lower() == "sqlite":
            normalized = self.sqlite_path.replace("\\", "/")
            return f"sqlite:///{normalized}"
        return self.mysql_url

    @property
    def mysql_url(self) -> str:
        return (
            f"mysql+pymysql://{self.mysql_user}:{self.mysql_password}"
            f"@{self.mysql_host}:{self.mysql_port}/{self.mysql_database}?charset=utf8mb4"
        )


settings = Settings()

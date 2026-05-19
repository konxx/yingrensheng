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

    app_host: str = Field(default="0.0.0.0", alias="APP_HOST")
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
    dashscope_api_key: str = Field(default="", alias="DASHSCOPE_API_KEY")
    dashscope_base_http_api_url: str = Field(default="https://dashscope.aliyuncs.com/api/v1", alias="DASHSCOPE_BASE_HTTP_API_URL")
    ai_text_api_key: str = Field(default="", alias="AI_TEXT_API_KEY")
    ai_text_model: str = Field(default="qwen3.6-plus", alias="AI_TEXT_MODEL")
    ai_image_api_key: str = Field(default="", alias="AI_IMAGE_API_KEY")
    ai_image_model: str = Field(default="qwen-image-2.0-pro", alias="AI_IMAGE_MODEL")
    ai_video_api_key: str = Field(default="", alias="AI_VIDEO_API_KEY")
    ai_video_model: str = Field(default="wan2.7-i2v", alias="AI_VIDEO_MODEL")
    ai_tts_api_key: str = Field(default="", alias="AI_TTS_API_KEY")
    ai_tts_model: str = Field(default="qwen-tts-realtime", alias="AI_TTS_MODEL")

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

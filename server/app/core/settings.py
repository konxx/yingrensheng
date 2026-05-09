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

    mysql_host: str = Field(default="127.0.0.1", alias="MYSQL_HOST")
    mysql_port: int = Field(default=3306, alias="MYSQL_PORT")
    mysql_user: str = Field(default="root", alias="MYSQL_USER")
    mysql_password: str = Field(default="change_me", alias="MYSQL_PASSWORD")
    mysql_database: str = Field(default="yingrensheng", alias="MYSQL_DATABASE")
    sqlite_path: str = Field(default=str(BASE_DIR / "yingrensheng.db"), alias="SQLITE_PATH")

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

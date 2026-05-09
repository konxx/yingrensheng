from pathlib import Path

from app.core.settings import settings

SERVER_DIR = Path(__file__).resolve().parent


def main() -> None:
    import uvicorn

    uvicorn.run(
        "app.main:app",
        host=settings.app_host,
        port=settings.app_port,
        reload=settings.app_reload,
        reload_dirs=[str(SERVER_DIR)],
    )


if __name__ == "__main__":
    main()

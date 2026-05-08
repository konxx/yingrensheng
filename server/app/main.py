from fastapi import FastAPI

from app.api.routes import api_router


app = FastAPI(
    title="YingRenSheng Server",
    version="0.1.0",
    description="Backend API for YingRenSheng Android app.",
)


@app.get("/health")
def health() -> dict[str, str]:
    return {"status": "ok"}


app.include_router(api_router, prefix="/api/v1")


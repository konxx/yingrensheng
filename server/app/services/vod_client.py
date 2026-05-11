import json
from pathlib import Path

from volcengine.vod.VodService import VodService
from volcengine.vod.models.request.request_vod_pb2 import VodUploadMediaRequest

from app.core.settings import settings


class VodStorageClient:
    def __init__(self) -> None:
        self.enabled = all(
            [
                settings.vod_access_key_id,
                settings.vod_secret_access_key,
                settings.vod_space_name,
            ],
        )
        self.service = VodService(settings.vod_region)
        if self.enabled:
            self.service.set_ak(settings.vod_access_key_id)
            self.service.set_sk(settings.vod_secret_access_key)

    def upload_file(self, file_path: str, object_name: str) -> str:
        if not self.enabled:
            raise RuntimeError("VOD credentials are not configured")
        path = Path(file_path)
        if not path.exists():
            raise FileNotFoundError(file_path)

        req = VodUploadMediaRequest()
        req.SpaceName = settings.vod_space_name
        req.FilePath = str(path)
        req.CallbackArgs = ""
        req.Functions = json.dumps([])
        req.FileName = object_name
        req.FileExtension = path.suffix or ".bin"
        req.StorageClass = 0
        req.UploadHostPrefer = ""

        resp = self.service.upload_media(req)
        if resp.ResponseMetadata.Error.Code != "":
            raise RuntimeError(str(resp.ResponseMetadata.Error))
        return resp.Result.Data.Vid


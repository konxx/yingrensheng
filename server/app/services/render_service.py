from pathlib import Path

from PIL import Image, ImageDraw, ImageFont

from app.core.settings import settings


class RenderService:
    def create_preview_video(
        self,
        project_id: str,
        title: str,
    ) -> tuple[str, str]:
        output_dir = Path(settings.storage_root).resolve() / "previews"
        output_dir.mkdir(parents=True, exist_ok=True)

        video_path = output_dir / f"{project_id}.gif"
        cover_path = output_dir / f"{project_id}.jpg"

        cover = self._create_frame(
            title=title,
            width=1280,
            height=720,
            offset=0,
            background=(247, 247, 247),
        )
        cover.convert("RGB").save(cover_path, format="JPEG", quality=92)

        frames = [
            self._create_frame(
                title=title,
                width=960,
                height=540,
                offset=offset,
                background=(247, 247, 247),
            )
            for offset in (-16, -8, 0, 8, 16, 8, 0)
        ]
        frames[0].save(
            video_path,
            save_all=True,
            append_images=frames[1:],
            duration=380,
            loop=0,
            format="GIF",
        )

        return video_path.as_uri(), cover_path.as_uri()

    def _create_frame(
        self,
        title: str,
        width: int,
        height: int,
        offset: int,
        background: tuple[int, int, int],
    ) -> Image.Image:
        image = Image.new("RGB", (width, height), background)
        draw = ImageDraw.Draw(image)
        font = self._load_font(44 if width >= 1000 else 34)
        small_font = self._load_font(18 if width >= 1000 else 14)

        # Soft card background
        card_margin = 72 if width >= 1000 else 42
        draw.rounded_rectangle(
            (
                card_margin,
                card_margin,
                width - card_margin,
                height - card_margin,
            ),
            radius=24 if width >= 1000 else 18,
            outline=(230, 230, 230),
            width=2,
            fill=(255, 255, 255),
        )

        # Poster block
        poster_size = 168 if width >= 1000 else 108
        poster_left = card_margin + 36
        poster_top = height // 2 - poster_size // 2
        draw.rounded_rectangle(
            (
                poster_left,
                poster_top,
                poster_left + poster_size,
                poster_top + poster_size,
            ),
            radius=26 if width >= 1000 else 18,
            fill=(232, 247, 238),
        )

        # Text area with gentle floating motion
        text_left = poster_left + poster_size + (44 if width >= 1000 else 28)
        title_text = title[:32]
        subtitle = "开发版预览"
        hint = "系统已生成预览资源，可继续走导出链路"

        title_box = draw.textbbox((0, 0), title_text, font=font)
        title_height = title_box[3] - title_box[1]
        title_y = height // 2 - title_height // 2 - 28 + offset

        draw.text((text_left, title_y), title_text, font=font, fill=(25, 25, 25))
        draw.text((text_left, title_y + title_height + 22), subtitle, font=small_font, fill=(7, 193, 96))
        draw.text((text_left, title_y + title_height + 58), hint, font=small_font, fill=(120, 120, 120))

        # Minimal QR-like corner marker
        qr_size = 78 if width >= 1000 else 48
        qr_left = width - card_margin - qr_size - 32
        qr_top = card_margin + 32
        self._draw_qr_badge(draw, qr_left, qr_top, qr_size)

        return image

    def _draw_qr_badge(self, draw: ImageDraw.ImageDraw, left: int, top: int, size: int) -> None:
        unit = size // 5
        color = (91, 107, 139)
        positions = [
            (0, 0),
            (3, 0),
            (0, 3),
            (2, 2),
            (4, 1),
            (4, 4),
        ]
        for x, y in positions:
            draw.rounded_rectangle(
                (
                    left + x * unit,
                    top + y * unit,
                    left + x * unit + unit - 4,
                    top + y * unit + unit - 4,
                ),
                radius=4,
                fill=color,
            )

    def _load_font(self, size: int) -> ImageFont.FreeTypeFont | ImageFont.ImageFont:
        candidates = [
            "C:/Windows/Fonts/msyh.ttc",
            "C:/Windows/Fonts/msyhbd.ttc",
            "C:/Windows/Fonts/simsun.ttc",
            "C:/Windows/Fonts/arial.ttf",
        ]
        for candidate in candidates:
            path = Path(candidate)
            if path.exists():
                try:
                    return ImageFont.truetype(str(path), size=size)
                except OSError:
                    continue
        return ImageFont.load_default()

from pathlib import Path
import textwrap

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

        return self._storage_url(video_path), self._storage_url(cover_path)

    def create_story_cover(
        self,
        project_id: str,
        title: str,
        subtitle: str,
        output_kind: str,
    ) -> str:
        output_dir = Path(settings.storage_root).resolve() / "works" / project_id
        output_dir.mkdir(parents=True, exist_ok=True)
        cover_path = output_dir / "cover.jpg"
        palette = {
            "CHARACTER_STORY": ((50, 31, 72), (216, 181, 103), (255, 248, 232)),
            "STORY_TEXT": ((238, 243, 239), (29, 82, 76), (31, 36, 35)),
            "COMIC_STORYBOARD": ((246, 244, 238), (139, 40, 48), (31, 31, 31)),
            "SHORT_VIDEO": ((20, 24, 31), (95, 178, 255), (245, 248, 255)),
        }.get(output_kind, ((247, 247, 247), (7, 193, 96), (25, 25, 25)))
        image = self._create_poster(
            title=title,
            subtitle=subtitle,
            width=1280,
            height=720,
            background=palette[0],
            accent=palette[1],
            foreground=palette[2],
        )
        image.convert("RGB").save(cover_path, format="JPEG", quality=92)
        return self._storage_url(cover_path)

    def create_comic_panel(
        self,
        project_id: str,
        index: int,
        title: str,
        summary: str,
        subtitle: str,
    ) -> str:
        output_dir = Path(settings.storage_root).resolve() / "works" / project_id / "comic"
        output_dir.mkdir(parents=True, exist_ok=True)
        image_path = output_dir / f"panel_{index + 1:02d}.jpg"
        width, height = (1280, 820) if index % 4 in (0, 3) else (960, 960)
        image = Image.new("RGB", (width, height), (245, 242, 233))
        draw = ImageDraw.Draw(image)
        title_font = self._load_font(40)
        body_font = self._load_font(26)
        small_font = self._load_font(22)

        margin = 48
        draw.rectangle((0, 0, width, height), outline=(32, 32, 32), width=10)
        draw.rounded_rectangle((margin, margin, width - margin, height - margin), radius=22, fill=(255, 252, 242), outline=(37, 37, 37), width=3)
        badge = f"{index + 1:02d}"
        draw.rounded_rectangle((margin + 24, margin + 24, margin + 112, margin + 82), radius=18, fill=(37, 37, 37))
        draw.text((margin + 48, margin + 36), badge, font=small_font, fill=(255, 252, 242), anchor="mm")

        scene_top = margin + 108
        scene_bottom = height - margin - 152
        draw.rounded_rectangle((margin + 28, scene_top, width - margin - 28, scene_bottom), radius=18, fill=(226, 221, 206), outline=(37, 37, 37), width=2)
        for stripe in range(0, width, 96):
            color = (215, 208, 191) if stripe % 192 == 0 else (235, 230, 216)
            draw.line((stripe, scene_top, stripe + 160, scene_bottom), fill=color, width=9)
        draw.ellipse((width // 2 - 92, scene_top + 68, width // 2 + 92, scene_top + 252), fill=(92, 62, 79), outline=(37, 37, 37), width=3)
        draw.rounded_rectangle((width // 2 - 138, scene_top + 236, width // 2 + 138, scene_bottom - 38), radius=40, fill=(160, 53, 61), outline=(37, 37, 37), width=3)
        draw.line((width // 2 - 170, scene_top + 310, width // 2 - 280, scene_top + 410), fill=(37, 37, 37), width=8)
        draw.line((width // 2 + 170, scene_top + 310, width // 2 + 280, scene_top + 410), fill=(37, 37, 37), width=8)

        text_y = height - margin - 130
        draw.text((margin + 28, text_y), title[:30], font=title_font, fill=(31, 31, 31))
        wrapped = "\n".join(textwrap.wrap(summary, width=34)[:2])
        draw.text((margin + 28, text_y + 52), wrapped, font=body_font, fill=(56, 56, 56), spacing=8)
        bubble_width = min(width - margin * 2 - 56, 760)
        bubble_left = width - margin - bubble_width - 28
        bubble_top = margin + 24
        draw.rounded_rectangle((bubble_left, bubble_top, width - margin - 28, bubble_top + 86), radius=20, fill=(255, 255, 255), outline=(37, 37, 37), width=2)
        draw.text((bubble_left + 20, bubble_top + 18), subtitle[:38], font=small_font, fill=(31, 31, 31))
        image.convert("RGB").save(image_path, format="JPEG", quality=90)
        return self._storage_url(image_path)

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

    def _create_poster(
        self,
        title: str,
        subtitle: str,
        width: int,
        height: int,
        background: tuple[int, int, int],
        accent: tuple[int, int, int],
        foreground: tuple[int, int, int],
    ) -> Image.Image:
        image = Image.new("RGB", (width, height), background)
        draw = ImageDraw.Draw(image)
        title_font = self._load_font(58)
        subtitle_font = self._load_font(30)
        small_font = self._load_font(22)

        margin = 76
        draw.rounded_rectangle((margin, margin, width - margin, height - margin), radius=32, outline=accent, width=4)
        draw.rectangle((margin + 34, margin + 34, margin + 210, height - margin - 34), fill=accent)
        draw.ellipse((margin + 72, margin + 82, margin + 172, margin + 182), fill=background, outline=foreground, width=3)
        draw.rounded_rectangle((margin + 56, margin + 196, margin + 190, height - margin - 86), radius=46, fill=background, outline=foreground, width=3)

        title_x = margin + 260
        title_y = height // 2 - 118
        for line in textwrap.wrap(title, width=18)[:2]:
            draw.text((title_x, title_y), line, font=title_font, fill=foreground)
            title_y += 74
        draw.text((title_x, title_y + 18), subtitle[:42], font=subtitle_font, fill=accent)
        draw.text((title_x, height - margin - 96), "YingRenSheng Final Work", font=small_font, fill=foreground)
        return image

    def _storage_url(self, path: Path) -> str:
        storage_root = Path(settings.storage_root).resolve()
        relative = path.resolve().relative_to(storage_root).as_posix()
        return f"/storage/{relative}"

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

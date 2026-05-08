from sqlalchemy import select

from app.db.models import UserAccountModel
from app.db.session import SessionLocal
from app.schemas.user import UserProfile


class UserRepository:
    def get_by_phone(self, phone: str) -> UserAccountModel | None:
        with SessionLocal() as session:
            stmt = select(UserAccountModel).where(UserAccountModel.phone == phone)
            return session.execute(stmt).scalar_one_or_none()

    def create_user(self, phone: str, nickname: str, password: str) -> UserProfile:
        with SessionLocal() as session:
            next_number = session.query(UserAccountModel).count() + 1
            model = UserAccountModel(
                user_id=f"user_{next_number:03d}",
                phone=phone,
                nickname=nickname,
                password=password,
                avatar_url="",
                role="user",
            )
            session.add(model)
            session.commit()
            session.refresh(model)
            return self._to_profile(model)

    def verify_user(self, phone: str, password: str) -> UserProfile | None:
        with SessionLocal() as session:
            stmt = select(UserAccountModel).where(
                UserAccountModel.phone == phone,
                UserAccountModel.password == password,
                UserAccountModel.role == "user",
            )
            model = session.execute(stmt).scalar_one_or_none()
            return self._to_profile(model) if model else None

    def verify_admin(self, username: str, password: str) -> UserAccountModel | None:
        with SessionLocal() as session:
            stmt = select(UserAccountModel).where(
                UserAccountModel.phone == username,
                UserAccountModel.password == password,
                UserAccountModel.role == "admin",
            )
            return session.execute(stmt).scalar_one_or_none()

    def list_users(self) -> list[UserAccountModel]:
        with SessionLocal() as session:
            stmt = select(UserAccountModel).where(UserAccountModel.role == "user").order_by(UserAccountModel.id.desc())
            return session.execute(stmt).scalars().all()

    @staticmethod
    def _to_profile(model: UserAccountModel) -> UserProfile:
        return UserProfile(
            userId=model.user_id,
            nickname=model.nickname,
            phone=model.phone,
            avatarUrl=model.avatar_url,
        )


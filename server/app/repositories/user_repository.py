from sqlalchemy import select

from app.db.models import UserAccountModel
from app.db.session import SessionLocal
from app.schemas.user import UserProfile


APP_ALLOWED_ROLES = {"user", "admin"}


class UserRepository:
    def get_by_username(self, username: str) -> UserAccountModel | None:
        with SessionLocal() as session:
            stmt = select(UserAccountModel).where(UserAccountModel.username == username)
            return session.execute(stmt).scalar_one_or_none()

    def create_user(self, username: str, nickname: str, password: str, email: str) -> UserProfile:
        with SessionLocal() as session:
            next_number = session.query(UserAccountModel).count() + 1
            model = UserAccountModel(
                user_id=f"user_{next_number:03d}",
                username=username,
                email=email,
                nickname=nickname,
                password=password,
                avatar_url="",
                role="user",
            )
            session.add(model)
            session.commit()
            session.refresh(model)
            return self._to_profile(model)

    def verify_user(self, username: str, password: str) -> UserProfile | None:
        with SessionLocal() as session:
            stmt = select(UserAccountModel).where(
                UserAccountModel.username == username,
                UserAccountModel.password == password,
                UserAccountModel.role.in_(APP_ALLOWED_ROLES),
            )
            model = session.execute(stmt).scalar_one_or_none()
            return self._to_profile(model) if model else None

    def get_by_user_id(self, user_id: str) -> UserProfile | None:
        with SessionLocal() as session:
            stmt = select(UserAccountModel).where(UserAccountModel.user_id == user_id)
            model = session.execute(stmt).scalar_one_or_none()
            return self._to_profile(model) if model else None

    def verify_admin(self, username: str, password: str) -> UserAccountModel | None:
        with SessionLocal() as session:
            stmt = select(UserAccountModel).where(
                UserAccountModel.username == username,
                UserAccountModel.password == password,
                UserAccountModel.role == "admin",
            )
            return session.execute(stmt).scalar_one_or_none()

    def list_users(self) -> list[UserAccountModel]:
        with SessionLocal() as session:
            stmt = select(UserAccountModel).where(UserAccountModel.role == "user").order_by(UserAccountModel.id.desc())
            return session.execute(stmt).scalars().all()

    def update_password(self, user_id: str, new_password: str) -> None:
        with SessionLocal() as session:
            stmt = select(UserAccountModel).where(UserAccountModel.user_id == user_id)
            model = session.execute(stmt).scalar_one_or_none()
            if model is None:
                return
            model.password = new_password
            session.commit()

    def update_profile(self, user_id: str, username: str, nickname: str, email: str) -> UserProfile | None:
        with SessionLocal() as session:
            stmt = select(UserAccountModel).where(UserAccountModel.user_id == user_id)
            model = session.execute(stmt).scalar_one_or_none()
            if model is None:
                return None
            model.username = username
            model.nickname = nickname
            model.email = email
            session.commit()
            session.refresh(model)
            return self._to_profile(model)

    @staticmethod
    def _to_profile(model: UserAccountModel) -> UserProfile:
        return UserProfile(
            userId=model.user_id,
            nickname=model.nickname,
            username=model.username,
            email=model.email,
            avatarUrl=model.avatar_url,
            role=model.role,
        )

import json

from sqlalchemy import select

from app.db.models import MemberPlanModel
from app.db.session import SessionLocal
from app.schemas.member import MemberPlanResponse


class MemberRepository:
    def list_plans(self) -> list[MemberPlanResponse]:
        with SessionLocal() as session:
            stmt = select(MemberPlanModel).order_by(MemberPlanModel.monthly_price.asc())
            models = session.execute(stmt).scalars().all()
            return [
                MemberPlanResponse(
                    planId=model.plan_id,
                    name=model.name,
                    monthlyPrice=model.monthly_price,
                    badge=model.badge,
                    summary=model.summary,
                    features=json.loads(model.features_json),
                )
                for model in models
            ]


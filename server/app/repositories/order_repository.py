from sqlalchemy import select

from app.core.time import now_iso
from app.db.models import OrderModel
from app.db.session import SessionLocal
from app.schemas.order import OrderResponse


class OrderRepository:
    def list(self, user_id: str | None = None) -> list[OrderResponse]:
        with SessionLocal() as session:
            stmt = select(OrderModel).order_by(OrderModel.id.desc())
            if user_id:
                stmt = stmt.where(OrderModel.user_id == user_id)
            models = session.execute(stmt).scalars().all()
            return [
                OrderResponse(
                    orderId=model.order_id,
                    title=model.title,
                    amountLabel=model.amount_label,
                    exportSpec=model.export_spec,
                    statusLabel=model.status_label,
                    createdAt=model.created_at,
                )
                for model in models
            ]

    def create(
        self,
        user_id: str,
        project_id: str | None,
        title: str,
        amount_label: str,
        export_spec: str,
        status_label: str,
    ) -> OrderResponse:
        with SessionLocal() as session:
            order_id = f"order_{session.query(OrderModel).count() + 1:03d}"
            model = OrderModel(
                order_id=order_id,
                user_id=user_id,
                project_id=project_id,
                title=title,
                amount_label=amount_label,
                export_spec=export_spec,
                status_label=status_label,
                created_at=now_iso(),
            )
            session.add(model)
            session.commit()
            session.refresh(model)
            return OrderResponse(
                orderId=model.order_id,
                title=model.title,
                amountLabel=model.amount_label,
                exportSpec=model.export_spec,
                statusLabel=model.status_label,
                createdAt=model.created_at,
            )

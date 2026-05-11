from pydantic import BaseModel, ConfigDict, Field


class OrderResponse(BaseModel):
    model_config = ConfigDict(populate_by_name=True, serialize_by_alias=True)

    order_id: str = Field(alias="orderId")
    title: str
    amount_label: str = Field(alias="amountLabel")
    export_spec: str = Field(alias="exportSpec")
    status_label: str = Field(alias="statusLabel")
    created_at: str = Field(alias="createdAt")


import pytest
from pydantic import ValidationError
from typing import get_args

from app.db.models.application import APPLICATION_STATUSES
from app.schemas.application import (
    ApplicationStatus,
    ApplicationStatusUpdate,
)


@pytest.mark.parametrize(
    "status",
    APPLICATION_STATUSES,
)
def test_application_status_update_accepts_supported_statuses(
    status,
):
    request = ApplicationStatusUpdate(status=status)

    assert request.status == status


def test_application_status_update_rejects_unknown_status():
    with pytest.raises(ValidationError):
        ApplicationStatusUpdate(status="unknown_status")


def test_application_status_contract_matches_model_statuses():
    schema_statuses = set(get_args(ApplicationStatus))
    model_statuses = set(APPLICATION_STATUSES)

    assert schema_statuses == model_statuses

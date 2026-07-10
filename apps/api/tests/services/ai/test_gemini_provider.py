from unittest.mock import Mock

import pytest
from google.genai import errors
from tenacity import wait_none

from app.services.ai.gemini_provider import GeminiProvider


def make_api_error(
    status_code: int,
) -> errors.APIError:

    return errors.APIError(
        status_code,
        {
            "error": {
                "code": status_code,
                "message": "Test error",
                "status": "TEST_ERROR",
            },
        },
        None,
    )


@pytest.fixture
def provider() -> GeminiProvider:

    provider = GeminiProvider.__new__(GeminiProvider)

    provider.client = Mock()

    return provider


def call_without_wait(
    provider: GeminiProvider,
):

    return provider._generate_json.retry_with(
        wait=wait_none(),
    )(
        provider,
        prompt="test prompt",
        schema=dict,
    )


def test_retries_temporary_errors_then_succeeds(
    provider: GeminiProvider,
) -> None:

    expected_result = {
        "result": "success",
    }

    response = Mock()
    response.parsed = expected_result

    provider.client.models.generate_content.side_effect = [
        make_api_error(503),
        make_api_error(503),
        response,
    ]

    result = call_without_wait(provider)

    assert result == expected_result

    assert (
        provider.client.models.generate_content.call_count
        == 3
    )


def test_stops_after_three_temporary_errors(
    provider: GeminiProvider,
) -> None:

    provider.client.models.generate_content.side_effect = (
        make_api_error(503)
    )

    with pytest.raises(errors.APIError) as exception_info:
        call_without_wait(provider)

    assert exception_info.value.code == 503

    assert (
        provider.client.models.generate_content.call_count
        == 3
    )


def test_does_not_retry_non_retryable_error(
    provider: GeminiProvider,
) -> None:

    provider.client.models.generate_content.side_effect = (
        make_api_error(400)
    )

    with pytest.raises(errors.APIError) as exception_info:
        call_without_wait(provider)

    assert exception_info.value.code == 400

    assert (
        provider.client.models.generate_content.call_count
        == 1
    )

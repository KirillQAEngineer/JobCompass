from app.core.config import settings

from app.services.ai.mock_provider import MockProvider
from app.services.ai.openai_provider import OpenAIProvider


def get_ai():

    provider = settings.ai_provider.lower()

    if provider == "mock":
        return MockProvider()

    if provider == "openai":
        return OpenAIProvider()

    raise ValueError(f"Unknown AI provider: {provider}")
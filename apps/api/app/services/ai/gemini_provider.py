from google import genai

from app.core.config import settings
from app.services.ai.base import AIProvider
from app.schemas.analysis import AnalysisResponse
from app.schemas.resume_profile import ResumeProfile


class GeminiProvider(AIProvider):

    def __init__(self):
        self.client = genai.Client(
            api_key=settings.gemini_api_key,
        )

    def analyze_resume(self, text: str) -> AnalysisResponse:

        # Пока заглушка
        return AnalysisResponse(
            summary="Gemini connected",
            score=100,
            strengths=["Gemini API works"],
            weaknesses=[],
            recommendations=[],
        )

    def build_resume_profile(self, text: str) -> ResumeProfile:

        # Пока заглушка
        return ResumeProfile(
            profession="QA Engineer",
            level="Senior",
            skills=["Python"],
            technologies=["Docker"],
            english_level="B1",
            preferred_roles=["QA Engineer"],
        )
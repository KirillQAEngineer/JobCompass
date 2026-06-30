from openai import OpenAI

from app.core.config import settings
from app.prompts.resume_review import RESUME_REVIEW_PROMPT
from app.prompts.resume_profile import RESUME_PROFILE_PROMPT
from app.schemas.resume_profile import ResumeProfile
from app.schemas.analysis import AnalysisResponse

from app.services.ai.base import AIProvider


class OpenAIProvider(AIProvider):

    def __init__(self):
        self.client = OpenAI(api_key=settings.openai_api_key)

    def analyze_resume(self, resume_text: str) -> AnalysisResponse:

        response = self.client.responses.create(
            model="gpt-4.1-mini",
            input=f"""
{RESUME_REVIEW_PROMPT}

{resume_text}
"""
        )

        return AnalysisResponse(
            summary=response.output_text,
            score=0,
            strengths=[],
            weaknesses=[],
            recommendations=[],
        )
    
    def build_resume_profile(self, resume_text: str) -> ResumeProfile:

        response = self.client.responses.parse(
            model="gpt-4.1-mini",
            input=f"""
    {RESUME_PROFILE_PROMPT}

    {resume_text}
    """,
            text_format=ResumeProfile,
    )

        return response.output_parsed
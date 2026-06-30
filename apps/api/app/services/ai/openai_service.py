from openai import OpenAI

from app.core.config import settings
from app.prompts.resume_review import RESUME_REVIEW_PROMPT

client = OpenAI(
    api_key=settings.openai_api_key,
)


def analyze_resume(text: str) -> str:
    response = client.responses.create(
        model="gpt-4.1-mini",
        input=f"""
{RESUME_REVIEW_PROMPT}

{text}
""",
    )

    return response.output_text
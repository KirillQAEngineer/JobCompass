from openai import OpenAI

from app.core.config import settings


client = OpenAI(
    api_key=settings.openai_api_key,
)


def analyze_resume(text: str) -> str:
    return f"""
РЕЗЮМЕ ПРОАНАЛИЗИРОВАНО

Длина текста: {len(text)} символов

Первые 300 символов:

{text[:300]}
"""

    return response.output_text
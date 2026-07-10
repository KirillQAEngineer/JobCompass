import logging

from google import genai
from google.genai import errors, types
from tenacity import (
    retry,
    retry_if_exception,
    stop_after_attempt,
    wait_exponential,
)

from app.core.config import settings
from app.prompts.job_match import JOB_MATCH_PROMPT
from app.prompts.resume_profile import RESUME_PROFILE_PROMPT
from app.prompts.resume_review import RESUME_REVIEW_PROMPT
from app.schemas.analysis import AnalysisResponse
from app.schemas.job import Job
from app.schemas.job_match import JobMatch
from app.schemas.resume_profile import ResumeProfile
from app.services.ai.base import AIProvider


logger = logging.getLogger(__name__)

RETRYABLE_STATUS_CODES = {
    429,
    500,
    502,
    503,
    504,
}


def is_retryable_gemini_error(
    exception: BaseException,
) -> bool:

    return (
        isinstance(exception, errors.APIError)
        and exception.code in RETRYABLE_STATUS_CODES
    )


class GeminiProvider(AIProvider):

    def __init__(self):
        self.client = genai.Client(
            api_key=settings.gemini_api_key,
        )

    @retry(
        retry=retry_if_exception(
            is_retryable_gemini_error,
        ),
        stop=stop_after_attempt(3),
        wait=wait_exponential(
            multiplier=1,
            min=1,
            max=4,
        ),
        reraise=True,
        before_sleep=lambda retry_state: logger.warning(
            "Gemini request failed temporarily. "
            "Retrying AI request. attempt=%s error=%s",
            retry_state.attempt_number,
            retry_state.outcome.exception(),
        ),
    )
    def _generate_json(
        self,
        prompt: str,
        schema,
    ):

        response = self.client.models.generate_content(
            model="gemini-2.5-flash",
            contents=prompt,
            config=types.GenerateContentConfig(
                response_mime_type="application/json",
                response_schema=schema,
            ),
        )

        return response.parsed

    def analyze_resume(
        self,
        text: str,
    ) -> AnalysisResponse:

        return self._generate_json(
            prompt=f"""
{RESUME_REVIEW_PROMPT}

{text}
""",
            schema=AnalysisResponse,
        )

    def build_resume_profile(
        self,
        text: str,
    ) -> ResumeProfile:

        return self._generate_json(
            prompt=f"""
{RESUME_PROFILE_PROMPT}

{text}
""",
            schema=ResumeProfile,
        )

    def match_job(
        self,
        resume_text: str,
        job: Job,
    ) -> JobMatch:

        return self._generate_json(
            prompt=f"""
{JOB_MATCH_PROMPT}

Resume:

{resume_text}

Job:

Title: {job.title}
Company: {job.company}
Location: {job.location}
Source: {job.source}
URL: {job.url}
""",
            schema=JobMatch,
        )

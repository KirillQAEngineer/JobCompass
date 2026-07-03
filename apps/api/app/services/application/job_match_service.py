from app.schemas.job import Job
from app.schemas.job_match import JobMatch

from app.services.ai.factory import get_ai


class JobMatchService:

    def __init__(self):
        self.ai = get_ai()

    def match(
        self,
        resume_text: str,
        job: Job,
    ) -> JobMatch:

        # Пока заглушка.
        # Позже здесь будет вызов OpenAI.

        return self.ai.match_job(
            resume_text,
            job,
        )
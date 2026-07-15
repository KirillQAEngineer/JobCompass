import logging

from google.genai import types

from app.db.models.resume_profile import ResumeProfile
from app.schemas.job import Job
from app.services.ai.factory import get_ai


logger = logging.getLogger(__name__)


class JobResumeService:

    def __init__(self):
        self.ai = None

    def generate(
        self,
        profile: ResumeProfile,
        job: Job,
    ) -> str:
        prompt = f"""
You are an expert resume writer.

Create a concise tailored resume for this specific vacancy.
Use only facts grounded in the candidate's existing profile and resume text.
Do not invent companies, dates, education, certifications, or achievements.
Keep plain text only.

Structure:
Professional Summary
Core Skills
Relevant Experience Focus
Tools and Technologies
Why This Candidate Fits

Candidate profile:
Profession: {profile.profession}
Level: {profile.level}
Skills: {profile.skills}
Technologies: {profile.technologies}
English level: {profile.english_level}
Preferred roles: {profile.preferred_roles}

Original resume text:
{profile.resume_text}

Target vacancy:
Title: {job.title}
Company: {job.company}
Location: {job.location}
Work format: {job.work_format or ""}
Description:
{job.description or ""}
"""

        try:
            response = self._get_ai().client.models.generate_content(
                model="gemini-2.5-flash",
                contents=prompt,
                config=types.GenerateContentConfig(
                    response_mime_type="text/plain",
                ),
            )

            text = (response.text or "").strip()

            if text:
                return text
        except Exception:
            logger.exception(
                "Gemini tailored resume generation failed for job: %s",
                job.title,
            )

        return self._fallback_resume(profile, job)

    def _fallback_resume(
        self,
        profile: ResumeProfile,
        job: Job,
    ) -> str:
        return (
            "Professional Summary\n"
            f"{profile.level} {profile.profession} targeting the "
            f"{job.title} role at {job.company}.\n\n"
            "Core Skills\n"
            f"{profile.skills or 'Skills are not specified.'}\n\n"
            "Tools and Technologies\n"
            f"{profile.technologies or 'Technologies are not specified.'}\n\n"
            "Relevant Experience Focus\n"
            "The resume should emphasize experience that matches the vacancy "
            "requirements, while keeping all details grounded in the original "
            "candidate profile.\n\n"
            "Why This Candidate Fits\n"
            f"The candidate profile is aligned with the target role "
            f"'{job.title}' and should be reviewed manually before sending."
        )

    def _get_ai(self):
        if self.ai is None:
            self.ai = get_ai()

        return self.ai

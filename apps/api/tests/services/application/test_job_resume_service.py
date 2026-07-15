from app.db.models.resume_profile import ResumeProfile
from app.schemas.job import Job
from app.services.application.job_resume_service import JobResumeService


def test_job_resume_service_fallback_uses_profile_and_job():
    service = JobResumeService.__new__(JobResumeService)
    profile = ResumeProfile(
        profession="QA Engineer",
        level="Senior",
        skills="API Testing, SQL, Regression Testing",
        technologies="Postman, Docker, PostgreSQL",
        english_level="B2",
        preferred_roles="QA Engineer",
        resume_text="Senior QA Engineer resume text",
    )
    job = Job(
        title="Senior QA Engineer",
        company="Acme",
        location="Remote",
        url="https://example.com/job",
        source="test",
        external_id="1",
        description="API and regression testing role",
    )

    resume = service._fallback_resume(profile, job)

    assert "Professional Summary" in resume
    assert "Senior QA Engineer" in resume
    assert "Acme" in resume

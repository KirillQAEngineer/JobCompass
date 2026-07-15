from app.services.application.resume_service import ResumeService


def test_resume_service_builds_fallback_profile_from_resume_text():
    service = ResumeService.__new__(ResumeService)

    profile = service._fallback_profile(
        "Senior QA Engineer with API testing, SQL, Postman, Docker and regression testing."
    )

    assert profile.profession == "QA Engineer"
    assert profile.level == "Senior"
    assert "API" in profile.skills
    assert "SQL" in profile.skills
    assert "Postman" in profile.technologies


def test_resume_service_builds_fallback_analysis():
    service = ResumeService.__new__(ResumeService)

    analysis = service._fallback_analysis("QA Engineer resume text")

    assert analysis.score == 60
    assert analysis.summary
    assert analysis.recommendations

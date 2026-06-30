from app.schemas.analysis import AnalysisResponse
from app.schemas.resume_profile import ResumeProfile

from app.services.ai.base import AIProvider


class MockProvider(AIProvider):

    def analyze_resume(self, text: str) -> AnalysisResponse:

        return AnalysisResponse(
            summary="Mock analysis",
            score=85,
            strengths=[
                "Strong backend experience",
                "Good API testing",
                "Microservices"
            ],
            weaknesses=[
                "No English level",
                "No automation experience"
            ],
            recommendations=[
                "Improve LinkedIn",
                "Add GitHub",
                "Study automation"
            ],
        )

    def build_resume_profile(self, text: str) -> ResumeProfile:

        return ResumeProfile(
            profession="QA Engineer",

            level="Senior",

            skills=[
                "Python",
                "SQL",
                "REST API",
                "Postman",
                "Docker",
            ],

            technologies=[
                "Kafka",
                "Kubernetes",
                "Redis",
                "Grafana",
            ],

            english_level="B1",

            preferred_roles=[
                "Senior QA Engineer",
                "QA Automation Engineer",
                "Backend QA Engineer",
            ],
        )
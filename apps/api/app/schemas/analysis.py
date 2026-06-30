from pydantic import BaseModel


class AnalysisResponse(BaseModel):
    summary: str

    score: int

    strengths: list[str]

    weaknesses: list[str]

    recommendations: list[str]
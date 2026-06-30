from pydantic import BaseModel
from app.schemas.analysis import AnalysisResponse
from app.schemas.resume_profile import ResumeProfile


class UploadResponse(BaseModel):
    filename: str
    characters: int
    text: str
    analysis: AnalysisResponse
    profile: ResumeProfile
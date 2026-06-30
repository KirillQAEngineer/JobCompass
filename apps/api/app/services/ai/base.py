from abc import ABC, abstractmethod

from app.schemas.analysis import AnalysisResponse
from app.schemas.resume_profile import ResumeProfile


class AIProvider(ABC):

    @abstractmethod
    def analyze_resume(self, text: str) -> AnalysisResponse:
        pass

    @abstractmethod
    def build_resume_profile(self, text: str) -> ResumeProfile:
        pass
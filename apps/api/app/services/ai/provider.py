from abc import ABC, abstractmethod

from app.schemas.analysis import AnalysisResponse


class AIProvider(ABC):

    @abstractmethod
    def analyze_resume(self, text: str) -> AnalysisResponse:
        pass
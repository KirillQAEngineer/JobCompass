from pathlib import Path
from uuid import uuid4

from fastapi import HTTPException, UploadFile
from google.genai import errors
from sqlalchemy.orm import Session

from app.core.config import settings
from app.db.models.resume_profile import ResumeProfile
from app.db.models.user import User
from app.db.repositories.resume_profile_repository import (
    ResumeProfileRepository,
)
from app.schemas.resume_profile import ResumeProfile as ResumeProfileSchema
from app.schemas.upload import UploadResponse
from app.services.ai.factory import get_ai
from app.services.parsers.parser import extract_text


UPLOAD_DIR = Path(settings.upload_dir)
UPLOAD_DIR.mkdir(exist_ok=True)


class ResumeService:

    def __init__(self, db: Session):
        self.db = db
        self.repository = ResumeProfileRepository(db)
        self.ai = get_ai()

    async def upload_resume(
        self,
        user: User,
        file: UploadFile,
    ) -> UploadResponse:

        filename = file.filename or ""

        self._validate_file(filename)

        file_path = await self._save_file(file)

        resume_text = self._extract_text(
            file_path,
        )

        try:
            profile = self._build_resume_profile(
                resume_text,
            )

            analysis = self._analyze_resume(
                resume_text,
            )
        except errors.APIError as exception:
            raise HTTPException(
                status_code=503,
                detail=(
                    "AI service is temporarily unavailable. "
                    "Please try again later."
                ),
            ) from exception

        self._save_resume_profile(
            user,
            profile,
            resume_text,
        )

        return UploadResponse(
            filename=filename,
            characters=len(resume_text),
            text=resume_text,
            analysis=analysis,
            profile=profile,
        )

    def _validate_file(
        self,
        filename: str,
    ) -> None:

        if not filename.lower().endswith((".pdf", ".docx")):
            raise HTTPException(
                status_code=400,
                detail="Only PDF and DOCX files are supported.",
            )

    async def _save_file(
        self,
        file: UploadFile,
    ) -> Path:

        suffix = Path(file.filename or "").suffix.lower()

        filename = f"{uuid4()}{suffix}"

        file_path = UPLOAD_DIR / filename

        content = await file.read()

        file_path.write_bytes(content)

        return file_path

    def _extract_text(
        self,
        file_path: Path,
    ) -> str:

        return extract_text(file_path)

    def _build_resume_profile(
        self,
        resume_text: str,
    ):

        return self.ai.build_resume_profile(
            resume_text,
        )

    def _analyze_resume(
        self,
        resume_text: str,
    ):

        return self.ai.analyze_resume(
            resume_text,
        )

    def _save_resume_profile(
        self,
        user: User,
        profile: ResumeProfileSchema,
        resume_text: str,
    ) -> ResumeProfile:

        return self.repository.upsert_from_resume(
            user_id=user.id,
            profile=profile,
            resume_text=resume_text,
        )

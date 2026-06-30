from pydantic import BaseModel


class ResumeProfile(BaseModel):
    profession: str

    level: str

    skills: list[str]

    technologies: list[str]

    english_level: str

    preferred_roles: list[str]
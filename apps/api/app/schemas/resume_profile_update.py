from pydantic import BaseModel, Field, field_validator


class ResumeProfileUpdate(BaseModel):
    profession: str = Field(
        min_length=1,
        max_length=255,
    )

    level: str = Field(
        min_length=1,
        max_length=100,
    )

    skills: list[str]

    technologies: list[str]

    english_level: str = Field(
        min_length=1,
        max_length=100,
    )

    preferred_roles: list[str]

    @field_validator(
        "profession",
        "level",
        "english_level",
    )
    @classmethod
    def normalize_required_text(
        cls,
        value: str,
    ) -> str:
        normalized = value.strip()

        if not normalized:
            raise ValueError("Value must not be empty")

        return normalized

    @field_validator(
        "skills",
        "technologies",
        "preferred_roles",
    )
    @classmethod
    def normalize_string_list(
        cls,
        values: list[str],
    ) -> list[str]:
        result: list[str] = []
        seen: set[str] = set()

        for value in values:
            normalized = value.strip()

            if not normalized:
                continue

            key = normalized.casefold()

            if key in seen:
                continue

            seen.add(key)
            result.append(normalized)

        return result

from pydantic import BaseModel

from app.schemas.job import Job


class JobMatch(BaseModel):
    job: Job

    match: int

    pros: list[str]

    cons: list[str]
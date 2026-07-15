from pydantic import BaseModel


class JobResumeResponse(BaseModel):
    resume: str

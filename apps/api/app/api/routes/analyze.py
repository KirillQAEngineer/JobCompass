from fastapi import APIRouter, File, UploadFile

router = APIRouter(
    prefix="/resume",
    tags=["Resume"],
)


@router.post("/analyze")
async def analyze_resume(
    file: UploadFile = File(...)
):
    return {
        "filename": file.filename,
        "status": "ok"
    }
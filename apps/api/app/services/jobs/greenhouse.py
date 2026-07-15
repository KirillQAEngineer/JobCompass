import requests

from app.schemas.job import Job
from app.services.jobs.base import JobProvider
from app.services.jobs.greenhouse_companies import GREENHOUSE_COMPANIES
from app.services.jobs.search_terms import matches_search_terms


class GreenhouseProvider(JobProvider):

    URL = "https://boards-api.greenhouse.io/v1/boards"

    def search(
        self,
        query: str,
    ) -> list[Job]:

        jobs = []

        for company_name, token in GREENHOUSE_COMPANIES:

            if not token:
                continue

            try:

                board_jobs = requests.get(
                    f"{self.URL}/{token}/jobs",
                    timeout=10,
                )

                if board_jobs.status_code != 200:
                    continue

                data = board_jobs.json()

                for item in data.get("jobs", []):

                    title = item.get("title", "")

                    searchable = " ".join(
                        [
                            title,
                            item.get("content") or "",
                            company_name,
                        ]
                    )

                    if not matches_search_terms(
                        searchable,
                        query,
                    ):
                        continue

                    location = "Remote"
                    locations = item.get("location")

                    if isinstance(locations, dict):
                        location = locations.get("name") or location

                    jobs.append(
                        Job(
                            title=title,
                            company=company_name,
                            location=location,
                            url=item.get("absolute_url", ""),
                            source="Greenhouse",
                            external_id=str(item.get("id", "")),
                            description=item.get("content"),
                            work_format=None,
                            published_at=item.get("updated_at"),
                        )
                    )

            except Exception:
                continue

        return jobs

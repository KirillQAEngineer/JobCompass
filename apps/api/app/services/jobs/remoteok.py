import requests

from app.schemas import Job
from app.services.jobs.base import JobProvider


class RemoteOKProvider(JobProvider):

    def search(self, query: str) -> list[Job]:
        
        print("=== REMOTEOK ===")

        response = requests.get(
            "https://remoteok.com/api",
            headers={
                "User-Agent": "CareerPilot"
            },
        )

        data = response.json()

        jobs = []

        for item in data[1:]:

            print(item.get("position"))

            title = item.get("position", "")

            if query.lower() not in title.lower():
                continue

            jobs.append(
                Job(
                    title=title,
                    company=item.get("company", ""),
                    location=item.get("location", "Remote"),
                    url=item.get("url", ""),
                    source="RemoteOK",
                )
            )

        return jobs
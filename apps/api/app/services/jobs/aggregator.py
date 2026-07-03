from app.schemas.job import Job

from app.services.jobs.remoteok import RemoteOKProvider
from app.services.jobs.remotive import RemotiveProvider


class JobsAggregator:

    def __init__(self):
        self.providers = [
            RemoteOKProvider(),
            RemotiveProvider(),
        ]

    def search(
        self,
        query: str,
    ) -> list[Job]:

        jobs = []

        for provider in self.providers:
            try:
                jobs.extend(
                    provider.search(query)
                )
            except Exception as error:
                print(
                    f"{provider.__class__.__name__}: {error}"
                )

        return jobs
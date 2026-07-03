from app.services.jobs.aggregator import JobsAggregator


def get_jobs_provider():
    return JobsAggregator()
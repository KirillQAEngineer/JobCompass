from app.services.jobs.search_terms import (
    detect_role_group,
    matches_search_terms,
    search_terms_for_query,
)


def test_detects_qa_role_from_senior_qa_query():
    assert detect_role_group("Senior QA Engineer") == "qa"


def test_expands_qa_query_to_common_role_variants():
    terms = search_terms_for_query("Senior QA Engineer")

    assert "qa" in terms
    assert "quality assurance" in terms
    assert "test automation" in terms
    assert "sdet" in terms


def test_matches_expanded_qa_role_variants():
    assert matches_search_terms(
        "SDET building API test automation",
        "Senior QA Engineer",
    )

    assert matches_search_terms(
        "Software Tester for mobile products",
        "Senior QA Engineer",
    )


def test_does_not_match_unrelated_role():
    assert not matches_search_terms(
        "Performance Marketing Manager",
        "Senior QA Engineer",
    )

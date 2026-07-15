import re


ROLE_SEARCH_TERMS = {
    "qa": (
        "qa",
        "quality assurance",
        "quality engineer",
        "quality analyst",
        "software tester",
        "test engineer",
        "test automation",
        "automation engineer",
        "sdet",
    ),
    "backend": (
        "backend",
        "back-end",
        "back end",
        "server-side",
        "api engineer",
        "python",
        "java",
        "golang",
        "node",
    ),
    "frontend": (
        "frontend",
        "front-end",
        "front end",
        "react",
        "vue",
        "angular",
        "typescript",
    ),
    "fullstack": (
        "fullstack",
        "full-stack",
        "full stack",
        "software engineer",
        "web developer",
    ),
    "mobile": (
        "mobile developer",
        "ios developer",
        "android developer",
        "flutter developer",
        "react native developer",
    ),
    "data": (
        "data engineer",
        "data scientist",
        "data analyst",
        "machine learning",
        "ml engineer",
        "analytics engineer",
    ),
    "devops": (
        "devops",
        "site reliability engineer",
        "sre",
        "platform engineer",
        "cloud engineer",
    ),
}


QUERY_ROLE_HINTS = {
    "qa": "qa",
    "quality assurance": "qa",
    "quality engineer": "qa",
    "software tester": "qa",
    "test engineer": "qa",
    "test automation": "qa",
    "sdet": "qa",
    "backend": "backend",
    "back-end": "backend",
    "back end": "backend",
    "frontend": "frontend",
    "front-end": "frontend",
    "front end": "frontend",
    "fullstack": "fullstack",
    "full-stack": "fullstack",
    "full stack": "fullstack",
    "mobile": "mobile",
    "ios": "mobile",
    "android": "mobile",
    "flutter": "mobile",
    "data engineer": "data",
    "data scientist": "data",
    "data analyst": "data",
    "machine learning": "data",
    "devops": "devops",
    "sre": "devops",
}


def normalize_text(value: str | None) -> str:
    if not value:
        return ""

    return re.sub(r"\s+", " ", value).strip().lower()


def contains_phrase(text: str, phrase: str) -> bool:
    pattern = (
        r"(?<!\w)"
        + re.escape(phrase)
        + r"(?!\w)"
    )

    return re.search(
        pattern,
        text,
        flags=re.IGNORECASE,
    ) is not None


def detect_role_group(query: str | None) -> str | None:
    normalized_query = normalize_text(query)

    if not normalized_query:
        return None

    ordered_hints = sorted(
        QUERY_ROLE_HINTS.items(),
        key=lambda item: len(item[0]),
        reverse=True,
    )

    for hint, role_group in ordered_hints:
        if contains_phrase(
            normalized_query,
            hint,
        ):
            return role_group

    return None


def search_terms_for_query(query: str | None) -> tuple[str, ...]:
    normalized_query = normalize_text(query)
    role_group = detect_role_group(normalized_query)

    if role_group is not None:
        return ROLE_SEARCH_TERMS[role_group]

    if not normalized_query:
        return ()

    terms = [
        term
        for term in re.split(r"[,\|/]+", normalized_query)
        if term.strip()
    ]

    return tuple(terms[:3] or [normalized_query])


def matches_search_terms(
    value: str,
    query: str | None,
) -> bool:
    terms = search_terms_for_query(query)

    if not terms:
        return True

    normalized_value = normalize_text(value)

    return any(
        contains_phrase(
            normalized_value,
            term,
        )
        for term in terms
    )

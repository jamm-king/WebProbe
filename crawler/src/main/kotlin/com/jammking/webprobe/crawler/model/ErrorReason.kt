package com.jammking.webprobe.crawler.model

enum class ErrorReason {
    SEARCH_FAILED,
    FETCH_FAILED,
    TRANSFORM_FAILED,
    PARSING_FAILED,
    ROBOTS_TXT_FAILED,
    UNKNOWN
}
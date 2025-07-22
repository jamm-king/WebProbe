package com.jammking.webprobe.crawler.model

enum class ErrorReason {
    SEARCH_FAILED,
    FETCH_FAILED,
    PARSING_FAILED,
    SUMMARY_FAILED,
    ROBOTS_TXT_FAILED,
    UNKNOWN
}
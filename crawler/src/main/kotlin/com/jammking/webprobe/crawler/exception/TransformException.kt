package com.jammking.webprobe.crawler.exception

import com.jammking.webprobe.common.exception.WebProbeException

class TransformException(
    val field: String
) : WebProbeException("Failed to extract $field from html")
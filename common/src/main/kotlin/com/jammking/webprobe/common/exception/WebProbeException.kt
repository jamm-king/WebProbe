package com.jammking.webprobe.common.exception

open class WebProbeException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)
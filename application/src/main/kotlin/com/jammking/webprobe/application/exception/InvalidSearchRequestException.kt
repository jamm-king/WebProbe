package com.jammking.webprobe.application.exception

import com.jammking.webprobe.common.exception.WebProbeException

class InvalidSearchRequestException(
    message: String
): WebProbeException(message)
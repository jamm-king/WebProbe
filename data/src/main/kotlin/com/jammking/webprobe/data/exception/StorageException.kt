package com.jammking.webprobe.data.exception

import com.jammking.webprobe.common.exception.WebProbeException

class StorageException(message: String, cause: Throwable? = null): WebProbeException(message, cause)
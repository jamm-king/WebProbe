package com.jammking.webprobe.data.service

import com.jammking.webprobe.data.entity.UserSeenUrl

interface UserSeenStorage {
    fun save(userId: String, url: String)
    fun isSeen(userId: String, url: String): Boolean
}
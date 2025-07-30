package com.jammking.webprobe.data.repository

import com.jammking.webprobe.data.entity.UserSeenUrl
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query

interface UserSeenUrlRepository: MongoRepository<UserSeenUrl, String> {
    fun findByUserIdAndUrl(userId: String, url: String): UserSeenUrl?
    fun findAllByUserId(userId: String): List<UserSeenUrl>
    fun deleteAllByUserId(userId: String)

    @Query(value = "{'userid': ?0}", fields = "{'url': 1, '_id': 0}")
    fun findOnlyUrlsByUserId(userId: String): List<String>
}
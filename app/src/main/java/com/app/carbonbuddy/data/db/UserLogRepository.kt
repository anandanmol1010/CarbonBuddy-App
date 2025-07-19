package com.app.carbonbuddy.data.db

import com.app.carbonbuddy.data.db.UserLogDao
import com.app.carbonbuddy.data.db.UserLogEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserLogRepository(private val userLogDao: UserLogDao) {
    suspend fun insertLog(log: UserLogEntity) = withContext(Dispatchers.IO) {
        userLogDao.insertLog(log)
    }
    suspend fun getAllLogs(): List<UserLogEntity> = withContext(Dispatchers.IO) {
        userLogDao.getAllLogs()
    }
    suspend fun getLogsByCategory(category: String): List<UserLogEntity> = withContext(Dispatchers.IO) {
        userLogDao.getLogsByCategory(category)
    }
    suspend fun deleteLog(log: UserLogEntity) = withContext(Dispatchers.IO) {
        userLogDao.deleteLog(log)
    }
}
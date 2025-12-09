package com.example.proyectofinal

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update


import java.util.List;

@Dao
interface AppDao {

    @Insert
    suspend fun registerUser(user: UserEntity): Long

    @Query("SELECT * FROM users WHERE username = :user AND password = :pass LIMIT 1")
    suspend fun login(user: String, pass: String): UserEntity?


    @Query("SELECT * FROM users WHERE username = :user LIMIT 1")
    suspend fun checkUserExists(user: String): UserEntity?


    @Query("UPDATE users SET password = :newPass WHERE username = :user")
    suspend fun updatePassword(user: String, newPass: String): Int

    @Update
    suspend fun updateProfile(user: UserEntity)

    @Query("UPDATE users SET nombre = :nombre, birthdate = :birthdate, password = :password WHERE username = :username")
    suspend fun updateUserData(username: String, nombre: String, birthdate: String, password: String)


    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    suspend fun getUserById(userId: Int): UserEntity?


    @Insert
    suspend fun insertCycle(cycle: CycleEntity): Long


    @Update
    suspend fun updateCycle(cycle: CycleEntity)


    @Query("SELECT * FROM cycles WHERE user_id = :userId ORDER BY start_date DESC")
    suspend fun getHistory(userId: Int): List<CycleEntity>


    @Query("SELECT * FROM cycles WHERE id = :cycleId")
    suspend fun getCycleById(cycleId: Int): CycleEntity?

    @Delete
    suspend fun deleteCycle(cycle: CycleEntity)

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): UserEntity?



}
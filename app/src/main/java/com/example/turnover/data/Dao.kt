package com.example.turnover.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY createdAt DESC")
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<TaskEntity>)

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteTaskById(id: String)

    @Query("DELETE FROM tasks")
    suspend fun deleteAllTasks()
}

@Dao
interface DailyRoutineDao {
    @Query("SELECT * FROM daily_routines ORDER BY createdAt DESC")
    fun getAllRoutines(): Flow<List<DailyRoutineEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutine(routine: DailyRoutineEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutines(routines: List<DailyRoutineEntity>)

    @Update
    suspend fun updateRoutine(routine: DailyRoutineEntity)

    @Query("DELETE FROM daily_routines WHERE id = :id")
    suspend fun deleteRoutineById(id: String)

    @Query("DELETE FROM daily_routines")
    suspend fun deleteAllRoutines()
}

@Dao
interface FailureDao {
    @Query("SELECT * FROM failures ORDER BY createdAt DESC")
    fun getAllFailures(): Flow<List<FailureEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFailure(failure: FailureEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFailures(failures: List<FailureEntity>)

    @Update
    suspend fun updateFailure(failure: FailureEntity)

    @Query("DELETE FROM failures WHERE id = :id")
    suspend fun deleteFailureById(id: String)

    @Query("DELETE FROM failures")
    suspend fun deleteAllFailures()
}

@Dao
interface AppMetaDao {
    @Query("SELECT * FROM app_meta WHERE id = 1 LIMIT 1")
    fun getAppMeta(): Flow<AppMetaEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveAppMeta(meta: AppMetaEntity)

    @Query("DELETE FROM app_meta")
    suspend fun deleteAllMeta()
}

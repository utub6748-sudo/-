package com.astro.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(@PrimaryKey(autoGenerate = true) val id: Long = 0, val title: String, val amount: Double, val currency: String, val category: String, val type: String, val createdAt: Long = System.currentTimeMillis())

@Entity(tableName = "habits")
data class HabitEntity(@PrimaryKey(autoGenerate = true) val id: Long = 0, val title: String, val cadence: String = "daily", val completedToday: Boolean = false, val streak: Int = 0)

@Entity(tableName = "goals")
data class GoalEntity(@PrimaryKey(autoGenerate = true) val id: Long = 0, val title: String, val targetAmount: Double? = null, val currentAmount: Double = 0.0, val currency: String = "EUR", val dueAt: Long? = null, val kind: String = "goal", val completed: Boolean = false)

@Entity(tableName = "categories")
data class CategoryEntity(@PrimaryKey(autoGenerate = true) val id: Long = 0, val name: String, val icon: String = "◉")

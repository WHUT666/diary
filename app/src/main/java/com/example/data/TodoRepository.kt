package com.example.data

import kotlinx.coroutines.flow.Flow

class TodoRepository(private val todoDao: TodoDao) {
    val allTodos: Flow<List<TodoItem>> = todoDao.getAllTodos()
    val pendingTodos: Flow<List<TodoItem>> = todoDao.getPendingTodos()
    val completedTodos: Flow<List<TodoItem>> = todoDao.getCompletedTodos()

    fun getTodosForDiary(diaryId: Long): Flow<List<TodoItem>> = todoDao.getTodosForDiary(diaryId)

    suspend fun insert(todo: TodoItem): Long = todoDao.insertTodo(todo)

    suspend fun insertAll(todos: List<TodoItem>) = todoDao.insertAll(todos)

    suspend fun update(todo: TodoItem) = todoDao.updateTodo(todo)

    suspend fun delete(todo: TodoItem) = todoDao.deleteTodo(todo)

    suspend fun toggleCompleted(todo: TodoItem) {
        val newStatus = !todo.isCompleted
        val completedTime = if (newStatus) System.currentTimeMillis() else null
        todoDao.updateCompletionStatus(todo.id, newStatus, completedTime)
    }
}

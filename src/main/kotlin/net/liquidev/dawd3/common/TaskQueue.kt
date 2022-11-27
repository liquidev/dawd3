package net.liquidev.dawd3.common

import java.util.function.Function

class TaskQueue<T, R> {
    private val tasks = arrayListOf<Function<T, R>>()

    fun enqueue(task: Function<T, R>) {
        tasks.add(task)
    }

    fun flush(argument: T) {
        for (task in tasks) {
            task.apply(argument)
        }
        tasks.clear()
    }
}
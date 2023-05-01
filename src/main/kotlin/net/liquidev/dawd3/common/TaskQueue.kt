package net.liquidev.dawd3.common

import java.util.concurrent.ConcurrentLinkedQueue
import java.util.function.Function

class TaskQueue<T, R> {
    private val tasks = ConcurrentLinkedQueue<Function<T, R>>()

    fun enqueue(task: Function<T, R>) {
        tasks.add(task)
    }

    fun flush(argument: T) {
        while (true) {
            val task = tasks.poll()
            if (task != null) {
                task.apply(argument)
            } else {
                break
            }
        }
    }
}
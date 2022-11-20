package net.liquidev.dawd3.common

import java.util.concurrent.Executor

class TaskQueue : Executor {
    private val tasks = arrayListOf<Runnable>()

    override fun execute(task: Runnable) {
        tasks.add(task)
    }

    fun flush() {
        for (task in tasks) {
            task.run()
        }
        tasks.clear()
    }
}
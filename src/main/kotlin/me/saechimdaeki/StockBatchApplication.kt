package me.saechimdaeki

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableAsync

@SpringBootApplication
class StockBatchApplication

fun main(args: Array<String>) {
    runApplication<StockBatchApplication>(*args)
}

package me.saechimdaeki.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.util.concurrent.ThreadPoolExecutor

@Configuration
class ThreadPoolConfig {

    @Bean
    fun stockThreadExecutor(): ThreadPoolTaskExecutor {
        return ThreadPoolTaskExecutor().apply {
            this.corePoolSize = 10
            this.maxPoolSize = 20
            setThreadNamePrefix("stockThread-")
            queueCapacity = 500
            setRejectedExecutionHandler(ThreadPoolExecutor.CallerRunsPolicy())
            initialize()
        }
    }
}
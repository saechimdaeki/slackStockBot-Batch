package me.saechimdaeki.config


import me.saechimdaeki.service.SlackService
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.launch.support.RunIdIncrementer
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.core.step.tasklet.Tasklet
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager


@Configuration
class StockJobConfig(
        private val jobRepository: JobRepository,
        private val platformTransactionManager: PlatformTransactionManager,
        private val slackService: SlackService,
) {

    @Bean
    fun stockGraph(): Step {
        val stepBuilderOne = StepBuilder("stepGraph", jobRepository)
        return stepBuilderOne
                .tasklet(stockGraphTasklet(), platformTransactionManager)
                .build()
    }

    @Bean
    fun koreaStock(): Step {
        val stepBuilderOne = StepBuilder("koreaStock", jobRepository)
        return stepBuilderOne
                .tasklet(koreaStockTasklet(), platformTransactionManager)
                .build()
    }

    @Bean
    fun globalStock(): Step {
        val stepBuilderOne = StepBuilder("globalStock", jobRepository)
        return stepBuilderOne
                .tasklet(globalStockTasklet(), platformTransactionManager)
                .build()
    }


    @Bean
    fun stockGraphTasklet(): Tasklet {
        return Tasklet { _, chunkContext ->
            val jobExecutionContext = chunkContext.stepContext
                    .stepExecution
                    .jobExecution
                    .executionContext

            val sendStockGraph = slackService.sendStockGraph()

            jobExecutionContext.put("stockGraph", sendStockGraph)

            RepeatStatus.FINISHED
        }
    }

    @Bean
    fun koreaStockTasklet(): Tasklet {
        return Tasklet { _, chunkContext ->

            val jobExecutionContext = chunkContext.stepContext
                    .stepExecution
                    .jobExecution
                    .executionContext


            val sendKoreaStockInfo = slackService.sendKoreaStockInfo()

            jobExecutionContext.put("koreaStock", sendKoreaStockInfo)

            RepeatStatus.FINISHED
        }
    }

    @Bean
    fun globalStockTasklet(): Tasklet {
        return Tasklet { _, chunkContext ->

            val jobExecutionContext = chunkContext.stepContext
                    .stepExecution
                    .jobExecution
                    .executionContext

            val stockGraph = jobExecutionContext.getString("stockGraph")
            val koreaStock = jobExecutionContext.getString("koreaStock")

            slackService.sendGlobalStockInfo(stockGraph, koreaStock)
            RepeatStatus.FINISHED
        }
    }

    @Bean
    fun demoJob(): Job {
        return JobBuilder("stockJob", jobRepository)
                .incrementer(RunIdIncrementer())
                .start(stockGraph())
                .next(koreaStock())
                .next(globalStock())
                .build()
    }
}
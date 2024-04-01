package me.saechimdaeki.config


import me.saechimdaeki.service.SlackService
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.launch.support.RunIdIncrementer
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.scope.context.ChunkContext
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
    fun investingAnalyze(): Step {
        val stepBuilderOne = StepBuilder("investingAnalyze", jobRepository)
        return stepBuilderOne
            .tasklet(investTasklet(), platformTransactionManager)
            .build()
    }


    @Bean
    fun investTasklet(): Tasklet {
        return Tasklet { _, _ ->
            slackService.sendInvestingAnalyze()
            RepeatStatus.FINISHED
        }
    }

    @Bean
    fun demoJob(): Job {
        return JobBuilder("stockJob", jobRepository)
            .incrementer(RunIdIncrementer())
            .start(investingAnalyze())
            .build()
    }
}
package me.saechimdaeki.service

import com.slack.api.Slack
import com.slack.api.methods.request.chat.ChatPostMessageRequest
import me.saechimdaeki.util.CrawlerUtil
import org.jsoup.nodes.Document
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service


@Service
class SlackService {

    @Value("\${slack.token}")
    lateinit var token: String

    @Value("\${slack.channel}")
    lateinit var channel: String

    @Value("\${stock.global}")
    lateinit var globalUrl: String

    @Value("\${stock.korea}")
    lateinit var koreaUrl: String

    @Value("\${stock.koreaGraph}")
    lateinit var koreaChart: String

    fun sendStockGraph(): String {
        return getCrawledInfo(koreaChart) { doc ->
            val koSPINow = doc.select("#KOSPI_now").first()?.text()
            val koSPIPointChange = doc.select("#KOSPI_change").first()?.text()
            val kosDAQNow = doc.select("#KOSDAQ_now").first()?.text()
            val kosDAQPointChange = doc.select("#KOSDAQ_change").first()?.text()
            """
            |:chart_with_upwards_trend: :chart_with_upwards_trend:
            |
            |성공적인 익절 기원합니다
            |코스피 지수입니다 : $koSPINow
            |코스피 변동 사항 : $koSPIPointChange
            |코스닥 지수입니다 : $kosDAQNow
            |코스닥 변동 사항 : $kosDAQPointChange
            """.trimMargin()
        }
    }

    fun sendKoreaStockInfo(): String {
        return getCrawledInfo(koreaUrl) { doc ->
            doc.select(".list_major a").joinToString(separator = "\n", prefix = "\n ===================================== \n :star2: 국내 증시 주요 뉴스 입니다 :star2:  \n") {
                val href = it.attr("href")
                "<$href|${it.text()}>\n"
            }
        }
    }

    fun sendGlobalStockInfo(graphMessage: String, koreaStockInfo: String) {
        val message = buildString {
            append(graphMessage).append(koreaStockInfo)
            append(getCrawledInfo(globalUrl) { doc ->
                doc.select(".news-item .news-tit a").joinToString(separator = "\n", prefix = "\n ===================================== \n :stars:  해외 증시 주요 뉴스 입니다 :stars:  \n") {
                    val href = it.attr("href")
                    "<$href|${it.text()}>\n"
                }
            })
        }
        sendSlackMessage(message)
    }

    @Async("stockThreadExecutor")
    fun sendSlackMessage(message: String) {
        val methods = Slack.getInstance().methods(token)
        val request = ChatPostMessageRequest.builder().channel(channel).text(message).build()
        methods.chatPostMessage(request)
    }

    private fun getCrawledInfo(url: String, transform: (doc: Document) -> String): String {
        val connection = CrawlerUtil.getConnection(url)
        val doc = connection.get()
        return transform(doc)
    }
}
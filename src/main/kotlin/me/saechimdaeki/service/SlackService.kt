package me.saechimdaeki.service

import com.slack.api.Slack
import com.slack.api.methods.request.chat.ChatPostMessageRequest
import me.saechimdaeki.util.CrawlerUtil
import org.jsoup.nodes.Document
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service


@Service
class SlackService(
    @Value("\${slack.token}") private val token: String,
    @Value("\${slack.channel}") private val channel: String,
    @Value("\${stock.global}") private val globalUrl: String,
    @Value("\${stock.korea}") private val koreaUrl: String,
    @Value("\${stock.koreaGraph}") private val koreaChart: String,
    @Value("\${stock.invest}") private val investingUrl: String
) {

    @Async("stockThreadExecutor")
    fun sendSlackMessage(message: String) {
        Slack.getInstance().methods(token).chatPostMessage(
            ChatPostMessageRequest.builder()
                .channel(channel)
                .text(message)
                .build()
        )
    }

    fun sendInvestingAnalyze() {
        val stockGraph = buildStockGraphMessage()
        val koreaStock = buildKoreaStockInfoMessage()
        val globalStock = buildGlobalStockInfoMessage()
        val investingAnalyze = buildInvestingAnalyzeMessage()
        sendSlackMessage("$stockGraph$koreaStock$globalStock$investingAnalyze")
    }

    private fun buildStockGraphMessage() = getCrawledInfo(koreaChart, ::parseStockGraph)

    private fun buildKoreaStockInfoMessage() = getCrawledInfo(koreaUrl, ::parseKoreaStockInfo)

    private fun buildGlobalStockInfoMessage() = getCrawledInfo(globalUrl, ::parseGlobalStockInfo)

    private fun buildInvestingAnalyzeMessage() = getCrawledInfo(investingUrl, ::parseInvestingAnalyze)

    private fun getCrawledInfo(url: String, transform: (Document) -> String): String {
        val doc = CrawlerUtil.getConnection(url).get()
        return transform(doc)
    }

    private fun parseStockGraph(doc: Document): String {
        val koSPINow = doc.select("#KOSPI_now").first()?.text()
        val koSPIPointChange = doc.select("#KOSPI_change").first()?.text()
        val kosDAQNow = doc.select("#KOSDAQ_now").first()?.text()
        val kosDAQPointChange = doc.select("#KOSDAQ_change").first()?.text()
        return """
            |:chart_with_upwards_trend: :chart_with_upwards_trend:
            |
            |성공적인 익절 기원합니다
            |코스피 지수입니다 : $koSPINow
            |코스피 변동 사항 : $koSPIPointChange
            |코스닥 지수입니다 : $kosDAQNow
            |코스닥 변동 사항 : $kosDAQPointChange
        """.trimMargin()
    }

    private fun parseKoreaStockInfo(doc: Document): String {
        return doc.select(".list_major a").joinToString(separator = "\n", prefix = "\n=====================================\n:star2: 국내 증시 주요 뉴스 입니다 :star2:\n") {
            val href = it.attr("href")
            "<$href|${it.text()}>\n"
        }
    }

    private fun parseGlobalStockInfo(doc: Document): String {
        return doc.select(".news-item .news-tit a").joinToString(separator = "\n", prefix = "\n=====================================\n:stars: 해외 증시 주요 뉴스 입니다 :stars:\n") {
            val href = it.attr("href")
            "<$href|${it.text()}>\n"
        }
    }

    private fun parseInvestingAnalyze(doc: Document): String {
        val linksWithDate = doc.select("#contentSection .textDiv a").fold("") { acc, element ->
            var href = element.attr("href")
            when {
                !href.startsWith("http") -> {
                    href = "$INVEST_URL$href"
                }
            }
            when {
                element.hasAttr("title") && element.attr("title").isNotEmpty() -> {
                    val date = element.parent().select(".articleDetails .date").text() 
                    acc + "<$href|${element.text()}> - $date\n"
                }
                else -> {
                    acc
                }
            }
        }
        return "\n=====================================\n:earth_americas: 인베스팅 주식 견해 입니다 :earth_americas:\n$linksWithDate"
    }

    companion object {
        const val INVEST_URL = "https://kr.investing.com"
    }
}
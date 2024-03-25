package me.saechimdaeki.service

import com.slack.api.Slack
import com.slack.api.methods.request.chat.ChatPostMessageRequest
import me.saechimdaeki.util.CrawlerUtil
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service


@Service
class SlackService {

    @Value("\${slack.token}")
    var token: String? = null

    @Value("\${slack.channel}")
    var channel: String? = null

    @Value("\${stock.global}")
    var globalUrl: String? = null

    @Value("\${stock.korea}")
    var koreaUrl: String? = null

    @Value("\${stock.koreaGraph}")
    var koreaChart: String? = null

    fun sendStockGraph() : String {
        koreaChart?.let {
            val connection = CrawlerUtil.getConnection(it)
            val doc = connection.get()
            val koSPINow = doc.select("#KOSPI_now").first()?.text()
            val koSPIPointChange = doc.select("#KOSPI_change").first()?.text()
            val kosDAQNow = doc.select("#KOSDAQ_now").first()?.text()
            val kosDAQPointChange = doc.select("#KOSDAQ_change").first()?.text()

            val chartMessage = ":chart_with_upwards_trend: :chart_with_upwards_trend: \n\n성공적인 익절 기원합니다 \n 코스피 지수입니다 : " +
                    "$koSPINow \n 코스피 변동 사항 : $koSPIPointChange \n " +
                    "코스닥 지수입니다 : $kosDAQNow \n 코스닥 변동 사항 : $kosDAQPointChange "
            return chartMessage
        }
        return ""
    }

    fun sendKoreaStockInfo() : String {
        koreaUrl?.let {
            val connection = CrawlerUtil.getConnection(it)
            val doc = connection.get()
            val infos = doc.select(".list_major a")
            val sb = StringBuilder()
            sb.append("\n ===================================== \n :star2: 국내 증시 주요 뉴스 입니다 :star2:  \n")
            infos.forEach { info ->
                val href = info.attr("href")
                sb.append("<$href|${info.text()}>\n")
            }
            return sb.toString()
        }
        return ""
    }

    fun sendGlobalStockInfo(graphMessage : String, koreaStockInfo : String) {
        val sb = StringBuilder()
        sb.append(graphMessage).append(koreaStockInfo)
        globalUrl?.let {
            val connection = CrawlerUtil.getConnection(it)
            val doc = connection.get()
            val newsItems = doc.select(".news-item")
            sb.append("\n ===================================== \n :stars:  해외 증시 주요 뉴스 입니다 :stars:  \n")
            for (newsItem in newsItems) {
                val item = newsItem.select(".news-tit a")
                val link = item.attr("href")
                sb.append("<$link|${item.text()}>\n")
            }
        }
        slackMessage(sb.toString())
    }

    @Async("stockThreadExecutor")
    fun slackMessage(message: String) {
        val methods = Slack.getInstance().methods(token)
        val request = ChatPostMessageRequest.builder()
                .channel(channel)
                .text(message)
                .build()
        methods.chatPostMessage(request)
    }
}
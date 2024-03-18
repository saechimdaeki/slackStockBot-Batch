package me.saechimdaeki.util

import org.jsoup.Connection
import org.jsoup.Jsoup

class CrawlerUtil {
    companion object {
        fun getConnection(url: String): Connection {
            return Jsoup.connect(url)
        }
    }
}
package me.saechimdaeki.service

import com.slack.api.Slack
import com.slack.api.methods.request.chat.ChatPostMessageRequest
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service

@Service
class AsyncService {

    @Async("stockThreadExecutor")
    fun sendSlackMessage(message: String, token:String, channel:String) {
        Slack.getInstance().methods(token).chatPostMessage(
            ChatPostMessageRequest.builder()
                .channel(channel)
                .text(message)
                .build()
        )
    }

}
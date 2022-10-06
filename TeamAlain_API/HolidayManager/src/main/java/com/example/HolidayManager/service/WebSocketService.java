package com.example.HolidayManager.service;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    public void sendMessage(final String topicSuffix) {
        messagingTemplate.convertAndSend("/topic/" + topicSuffix, "sample text");
    }
}

package edu.meialua.kidsgrace.service;

import edu.meialua.kidsgrace.config.KafkaTopics;
import edu.meialua.kidsgrace.model.LogEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducerService {

    private final KafkaTemplate<String, LogEvent> kafkaTemplate;

    public KafkaProducerService(KafkaTemplate<String, LogEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }


//    public void sendLog(LogEvent event){
//        kafkaTemplate.send("logs", event);
//    }

    public void sendUserEvent(LogEvent logEvent) {
        kafkaTemplate.send(KafkaTopics.USERS_EVENTS, logEvent);
    }

    public void sendToyEvent(LogEvent logEvent) {
        kafkaTemplate.send(KafkaTopics.TOYS_EVENTS, logEvent);
    }
}

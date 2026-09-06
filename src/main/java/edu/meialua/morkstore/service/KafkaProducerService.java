package edu.meialua.morkstore.service;

import edu.meialua.morkstore.config.KafkaTopics;
import edu.meialua.morkstore.model.LogEvent;
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

    public void sendProductEvent(LogEvent logEvent) {
        kafkaTemplate.send(KafkaTopics.PRODUCTS_EVENTS, logEvent);
    }

    public void sendOrderEvent(LogEvent logEvent) {
        kafkaTemplate.send(KafkaTopics.ORDERS_EVENTS, logEvent);
    }
}

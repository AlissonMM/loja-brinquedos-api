package edu.meialua.kidsgrace.service;

import edu.meialua.kidsgrace.model.LogEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducerService {

    private final KafkaTemplate<String, LogEvent> kafkaTemplate;

    public KafkaProducerService(KafkaTemplate<String, LogEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendLog(LogEvent event){
        kafkaTemplate.send("logs", event);
    }
}

package edu.meialua.kidsgrace.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogEvent {

    private String action;

    private String entity;

    private Long entityId;

    private String user;

    private String description;

    private LocalDateTime timestamp;
}

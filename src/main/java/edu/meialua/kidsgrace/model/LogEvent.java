package edu.meialua.kidsgrace.model;

import edu.meialua.kidsgrace.adapters.in.enums.Action;
import edu.meialua.kidsgrace.adapters.in.enums.EntityType;
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

    private Action action;

    private EntityType entity;

    private Long entityId;

    private String user;

    private String description;

    private LocalDateTime timestamp;
}

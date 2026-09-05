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

    // Campos opcionais, usados hoje só pelos eventos de venda por item
    // (Action.SALE) — permitem à analytics cortar receita/top produtos por
    // categoria, marca e quantidade sem precisar consultar o catálogo.
    // Ficam null em todos os outros eventos (REGISTER, LOGIN, UPDATE, etc.).
    private String category;

    private String brand;

    private Float unitValue;

    private Integer quantity;
}

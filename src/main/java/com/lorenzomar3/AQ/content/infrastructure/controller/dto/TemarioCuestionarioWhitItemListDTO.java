package com.lorenzomar3.AQ.content.infrastructure.controller.dto;

import java.time.LocalDateTime;
import java.util.List;

// Reemplazando la extensión de clase por una composición más robusta en el mundo Records, manteniendo todos los campos y añadiendo el listado específico.
public record TemarioCuestionarioWhitItemListDTO(Long id, String name, LocalDateTime creationDate, List<AResponderItemListDTO> itemList) {}
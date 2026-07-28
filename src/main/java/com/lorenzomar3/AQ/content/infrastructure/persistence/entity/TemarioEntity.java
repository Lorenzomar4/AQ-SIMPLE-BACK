package com.lorenzomar3.AQ.content.infrastructure.persistence.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "temario")
@Getter
@Setter
@NoArgsConstructor
public class TemarioEntity extends AResponderEntity {
}

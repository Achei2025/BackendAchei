package br.gov.achei.achei.models;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "object_properties")
public class ObjectProperty {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "`key`", nullable = false)
    private String key; // Nome da propriedade específica: IMEI, Número do Chassi, etc.

    @Column(name = "value", nullable = false)
    private String value; // Valor da propriedade: Exemplo de valor para IMEI ou Chassi

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "object_id", nullable = false)
    @JsonBackReference
    private GenericObject object; // Referência ao objeto principal
}

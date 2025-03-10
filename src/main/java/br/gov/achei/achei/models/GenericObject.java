/*
 * Achei: Stolen Object Tracking System.
 * Copyright (C) 2025  Team Achei
 * 
 * This file is part of Achei.
 * 
 * Achei is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Achei is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Achei.  If not, see <https://www.gnu.org/licenses/>.
 * 
 * Contact information: teamachei.2024@gmail.com
*/

package br.gov.achei.achei.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@Entity
@Table(name = "generic_objects")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class GenericObject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", nullable = false)
    private String name; // Nome genérico do objeto

    @Column(name = "category", nullable = false)
    private String category; // Categoria do objeto: Eletrônicos, Veículos, Outros

    @Column(name = "description", length = 500)
    private String description; // Descrição geral do objeto

    @Column(name = "serial_number", length = 100)
    private String serialNumber; // Número de série do objeto

    @Column(name = "identification_code", length = 100)
    private String identificationCode; // Código único de identificação do objeto

    @Column(name = "brand")
    private String brand; // Marca do objeto

    @Column(name = "model")
    private String model; // Modelo do objeto

    @Column(name = "acquisition_date")
    private LocalDateTime acquisitionDate; // Data de aquisição do objeto

    @Column(name = "invoice", length = 255)
    private String invoice; // Caminho para a nota fiscal armazenada

    @Column(name = "qr_code", length = 255)
    private String qrCode; // Caminho para o QR Code gerado

    @Column(name = "chip_identifier", length = 100)
    private String chipIdentifier; // Identificador de chip simples

    @Column(name = "image")
    private String image; // URL ou caminho para a imagem do objeto

    @Column(name = "status", length = 50)
    private String status; // Status do objeto: Seguro, Furtado, etc.

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "citizen_id", nullable = false)
    @JsonBackReference
    private Citizen citizen; // Cidadão associado ao objeto

    @OneToMany(mappedBy = "object", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<ObjectProperty> properties; // Propriedades dinâmicas específicas do objeto

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt; // Timestamp de criação do objeto

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt; // Timestamp de última atualização do objeto

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}

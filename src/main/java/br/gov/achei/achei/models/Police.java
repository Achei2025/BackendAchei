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

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import br.gov.achei.achei.utils.EncryptionUtil;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "police")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Police {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", nullable = false, length = 256)
    private String name;

    @Column(name = "registration", nullable = false, unique = true, length = 256)
    private String registration;

    @Column(name = "occupation", nullable = false, length = 256)
    private String occupation;

    @Column(name = "gender", nullable = false, length = 256)
    private String gender;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void encryptDataBeforePersist() {
        this.name = EncryptionUtil.encrypt(this.name);
        this.registration = EncryptionUtil.encrypt(this.registration);
        this.occupation = EncryptionUtil.encrypt(this.occupation);
        this.gender = EncryptionUtil.encrypt(this.gender);
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void encryptDataBeforeUpdate() {
        this.name = EncryptionUtil.encrypt(this.name);
        this.registration = EncryptionUtil.encrypt(this.registration);
        this.occupation = EncryptionUtil.encrypt(this.occupation);
        this.gender = EncryptionUtil.encrypt(this.gender);
        this.updatedAt = LocalDateTime.now();
    }

    @PostLoad
    public void decryptDataAfterLoad() {
        try {
            if (EncryptionUtil.isEncrypted(this.name)) this.name = EncryptionUtil.decrypt(this.name);
            if (EncryptionUtil.isEncrypted(this.registration)) this.registration = EncryptionUtil.decrypt(this.registration);
            if (EncryptionUtil.isEncrypted(this.occupation)) this.occupation = EncryptionUtil.decrypt(this.occupation);
            if (EncryptionUtil.isEncrypted(this.gender)) this.gender = EncryptionUtil.decrypt(this.gender);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

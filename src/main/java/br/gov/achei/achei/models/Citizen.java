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

import br.gov.achei.achei.utils.EncryptionUtil;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@Entity
@Table(name = "citizens")
public class Citizen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "cpf", nullable = false, unique = true, length = 256)
    private String cpf;

    @Column(name = "full_name", nullable = false, length = 256)
    private String fullName;

    @Column(name = "rg", nullable = false, unique = true, length = 256)
    private String rg;

    @Column(name = "email", nullable = false, unique = true, length = 256)
    private String email;

    @Column(name = "phone", nullable = false, length = 256)
    private String phone;

    @Column(name = "password", nullable = false, length = 256)
    private String password;

    @Column(name = "birth_date", nullable = false, length = 256)
    private String birthDate;

    @Column(name = "gender", nullable = false, length = 256)
    private String gender;

    @Column(name = "image", length = 256)
    private String image;

    @OneToOne(mappedBy = "citizen", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference
    private Address address;

    @OneToMany(mappedBy = "citizen", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<GenericObject> objects;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void encryptDataBeforePersist() {
        this.cpf = EncryptionUtil.encrypt(this.cpf);
        this.fullName = EncryptionUtil.encrypt(this.fullName);
        this.rg = EncryptionUtil.encrypt(this.rg);
        this.email = EncryptionUtil.encrypt(this.email);
        this.phone = EncryptionUtil.encrypt(this.phone);
        this.password = EncryptionUtil.hashPassword(this.password);
        this.image = EncryptionUtil.encrypt(this.image);
        this.gender = EncryptionUtil.encrypt(this.gender);
        this.birthDate = EncryptionUtil.encrypt(this.birthDate);
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void encryptDataBeforeUpdate() {
        this.cpf = EncryptionUtil.encrypt(this.cpf);
        this.fullName = EncryptionUtil.encrypt(this.fullName);
        this.rg = EncryptionUtil.encrypt(this.rg);
        this.email = EncryptionUtil.encrypt(this.email);
        this.phone = EncryptionUtil.encrypt(this.phone);
        this.image = EncryptionUtil.encrypt(this.image);
        this.gender = EncryptionUtil.encrypt(this.gender);
        this.birthDate = EncryptionUtil.encrypt(this.birthDate);
        if (!EncryptionUtil.checkPassword(this.password, this.password)) {
            this.password = EncryptionUtil.hashPassword(this.password);
        }
        this.updatedAt = LocalDateTime.now();
    }

    @PostLoad
    public void decryptDataAfterLoad() {
        try {
            if (EncryptionUtil.isEncrypted(this.cpf)) this.cpf = EncryptionUtil.decrypt(this.cpf);
            if (EncryptionUtil.isEncrypted(this.fullName)) this.fullName = EncryptionUtil.decrypt(this.fullName);
            if (EncryptionUtil.isEncrypted(this.rg)) this.rg = EncryptionUtil.decrypt(this.rg);
            if (EncryptionUtil.isEncrypted(this.email)) this.email = EncryptionUtil.decrypt(this.email);
            if (EncryptionUtil.isEncrypted(this.phone)) this.phone = EncryptionUtil.decrypt(this.phone);
            if (EncryptionUtil.isEncrypted(this.image)) this.image = EncryptionUtil.decrypt(this.image);
            if (EncryptionUtil.isEncrypted(this.gender)) this.gender = EncryptionUtil.decrypt(this.gender);
            if (EncryptionUtil.isEncrypted(this.birthDate)) this.birthDate = EncryptionUtil.decrypt(this.birthDate);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}


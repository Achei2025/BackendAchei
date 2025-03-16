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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import br.gov.achei.achei.utils.EncryptionUtil;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@Entity
@Table(name = "cases")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Case {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "status", nullable = false, length = 50)
    private String status;

    @Column(name = "crime_type", nullable = false, length = 100)
    private String crimeType;

    @Column(name = "reported_at", nullable = false)
    private LocalDateTime reportedAt;

    @Column(name = "location", nullable = false, length = 255)
    private String location;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "citizen_id", nullable = false)
    @JsonIgnoreProperties({"cpf", "fullName", "rg", "email", "phone", "birthDate", "gender", "occupation", "image", "address", "objects", "cases", "user", "createdAt", "updatedAt"})
    private Citizen citizen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "police_id")
    private Police police;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "object_id", nullable = false)
    private GenericObject object;

    @OneToMany(mappedBy = "caseReference", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference(value = "case-messages")
    @JsonIgnore
    private List<Message> messages;

    @OneToMany(mappedBy = "caseReference", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference(value = "case-comments")
    private List<Comment> comments;

    @PrePersist
    public void onPrePersist() {
        this.reportedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onPreUpdate() {
        if ("Resolvido".equalsIgnoreCase(this.status)) {
            this.closedAt = LocalDateTime.now();
        } else {
            this.closedAt = null;
        }
    }

    @JsonIgnore
    public boolean canViewMessages(String username, String policeRegistration) {
        return this.getCitizen().getUser().getUsername().equals(EncryptionUtil.encrypt(username)) || 
            (this.getPolice() != null && this.getPolice().getRegistration().equals(EncryptionUtil.encrypt(policeRegistration)));
    }

    public List<Message> getFilteredMessages(String username, String policeRegistration) {
        if (this.canViewMessages(username, policeRegistration)) {
            return this.messages;
        }
        return null;
    }
}

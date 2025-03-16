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

package br.gov.achei.achei.controllers;

import br.gov.achei.achei.models.Case;
import br.gov.achei.achei.models.Citizen;
import br.gov.achei.achei.models.GenericObject;
import br.gov.achei.achei.models.Police;
import br.gov.achei.achei.services.CitizenService;
import br.gov.achei.achei.utils.EncryptionUtil;
import br.gov.achei.achei.utils.JwtUtil;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/citizens")
public class CitizenController {

    private final CitizenService citizenService;
    private final JwtUtil jwtUtil;

    public CitizenController(CitizenService citizenService, JwtUtil jwtUtil) {
        this.citizenService = citizenService;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping("/me")
    public ResponseEntity<Citizen> getAuthenticatedCitizen(@RequestHeader("Authorization") String token) {
        String username = jwtUtil.extractUsername(token.substring(7));

        Citizen citizen = citizenService.getCitizenByUsername(username)
                .orElseThrow(() -> new NoSuchElementException("Citizen not found"));

        citizen.decryptDataAfterLoad();

        return ResponseEntity.ok(citizen);
    }

    @PostMapping
    public ResponseEntity<Citizen> createCitizen(@RequestBody Citizen citizen) {
        if (citizen.getUser() == null || citizen.getUser().getPassword() == null || citizen.getUser().getPassword().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }

        String rawPassword = citizen.getUser().getPassword();

        Citizen savedCitizen = citizenService.createCitizen(citizen, rawPassword);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedCitizen);
    }

    @PutMapping("/me")
    public ResponseEntity<Citizen> updateAuthenticatedCitizen(@RequestHeader("Authorization") String token, @RequestBody Citizen citizen) {
        String username = jwtUtil.extractUsername(token.substring(7));
        Citizen existingCitizen = citizenService.getCitizenByUsername(username)
                .orElseThrow(() -> new NoSuchElementException("Citizen not found"));

        Citizen updatedCitizen = citizenService.updateCitizen(existingCitizen.getId(), citizen);
        return ResponseEntity.ok(updatedCitizen);
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteAuthenticatedCitizen(@RequestHeader("Authorization") String token) {
        String username = jwtUtil.extractUsername(token.substring(7));
        Citizen citizen = citizenService.getCitizenByUsername(username)
                .orElseThrow(() -> new NoSuchElementException("Citizen not found"));

        citizenService.deleteCitizen(citizen.getId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me/objects")
    public ResponseEntity<List<GenericObject>> getAuthenticatedCitizenObjects(@RequestHeader("Authorization") String token) {
        String username = jwtUtil.extractUsername(token.substring(7));
        Citizen citizen = citizenService.getCitizenByUsername(username)
                .orElseThrow(() -> new NoSuchElementException("Citizen not found"));
        return ResponseEntity.ok(citizen.getObjects());
    }

    @GetMapping("/me/cases")
    public ResponseEntity<List<Case>> getAuthenticatedCitizenCases(@RequestHeader("Authorization") String token) {
        String username = jwtUtil.extractUsername(token.substring(7));
        Citizen citizen = citizenService.getCitizenByUsername(username)
                .orElseThrow(() -> new NoSuchElementException("Citizen not found"));
        
        if (EncryptionUtil.isEncrypted(citizen.getAnonymousName())) {
            citizen.decryptDataAfterLoad();
        }

        citizen.getCases().forEach(caseItem -> {
            if (caseItem.getPolice() != null) {
                Police filteredPolice = new Police();
                filteredPolice.setId(caseItem.getPolice().getId());
                filteredPolice.setName(EncryptionUtil.decrypt(caseItem.getPolice().getName()));
                caseItem.setPolice(filteredPolice);
            }
        });

        return ResponseEntity.ok(citizen.getCases());
    }
}

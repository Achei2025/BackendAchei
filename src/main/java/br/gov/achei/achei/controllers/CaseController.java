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
import br.gov.achei.achei.services.CaseService;
import br.gov.achei.achei.services.CitizenService;
import br.gov.achei.achei.utils.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/cases")
public class CaseController {

    private final CaseService caseService;
    private final CitizenService citizenService;
    private final JwtUtil jwtUtil;

    public CaseController(CaseService caseService, CitizenService citizenService, JwtUtil jwtUtil) {
        this.caseService = caseService;
        this.citizenService = citizenService;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping
    public ResponseEntity<List<Case>> getAllCases() {
        try {
            List<Case> cases = caseService.getAllCases();
            return ResponseEntity.ok(cases);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Case> getCaseById(@PathVariable Long id) {
        try {
            Case publicCase = caseService.getCaseById(id);
            return ResponseEntity.ok(publicCase);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PostMapping("/me")
    public ResponseEntity<Case> createCase(
            @RequestHeader("Authorization") String token,
            @RequestBody Case caseData,
            @RequestParam Long objectId) {
        try {
            String username = jwtUtil.extractUsername(token.substring(7));
            Citizen citizen = citizenService.getCitizenByUsername(username)
                    .orElseThrow(() -> new NoSuchElementException("Citizen not found"));

            Case createdCase = caseService.createCase(caseData, citizen.getId(), objectId);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdCase);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PutMapping("/{id}/me")
    public ResponseEntity<Case> updateCase(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token,
            @RequestBody Case updatedCase,
            @RequestParam(required = false) Long policeId,
            @RequestParam(required = false) Long objectId) {
        try {
            String username = jwtUtil.extractUsername(token.substring(7));
            Citizen citizen = citizenService.getCitizenByUsername(username)
                    .orElseThrow(() -> new NoSuchElementException("Citizen not found"));

            Case updated = caseService.updateCase(id, citizen.getId(), updatedCase, policeId, objectId);
            return ResponseEntity.ok(updated);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @DeleteMapping("/{id}/me")
    public ResponseEntity<Void> deleteCase(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {
        try {
            String username = jwtUtil.extractUsername(token.substring(7));
            Citizen citizen = citizenService.getCitizenByUsername(username)
                    .orElseThrow(() -> new NoSuchElementException("Citizen not found"));

            caseService.deleteCase(id, citizen.getId());
            return ResponseEntity.noContent().build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }
}

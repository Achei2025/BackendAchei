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

import br.gov.achei.achei.models.Police;
import br.gov.achei.achei.services.PoliceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@RestController
@RequestMapping("/api/police")
public class PoliceController {

    private final PoliceService policeService;

    public PoliceController(PoliceService policeService) {
        this.policeService = policeService;
    }

    @GetMapping
    public List<Police> getAllPolice() {
        return policeService.getAllPolice();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Police> getPoliceById(@PathVariable Long id) {
        Optional<Police> police = policeService.getPoliceById(id);
        return police.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PostMapping
    public ResponseEntity<Police> createPolice(@RequestBody Police police) {
        Police savedPolice = policeService.createPolice(police);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedPolice);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Police> updatePolice(@PathVariable Long id, @RequestBody Police police) {
        try {
            Police updatedPolice = policeService.updatePolice(id, police);
            return ResponseEntity.ok(updatedPolice);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePolice(@PathVariable Long id) {
        policeService.deletePolice(id);
        return ResponseEntity.noContent().build();
    }
}
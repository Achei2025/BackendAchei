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

import br.gov.achei.achei.models.GenericObject;
import br.gov.achei.achei.services.GenericObjectService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/objects")
public class GenericObjectController {

    private final GenericObjectService genericObjectService;

    public GenericObjectController(GenericObjectService genericObjectService) {
        this.genericObjectService = genericObjectService;
    }

    @GetMapping
    public List<GenericObject> getAllObjects() {
        // Retorna todos os objetos
        return genericObjectService.getAllObjects();
    }

    @GetMapping("/{id}")
    public ResponseEntity<GenericObject> getObjectById(@PathVariable Long id) {
        // Retorna um objeto específico pelo ID
        return genericObjectService.getObjectById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PostMapping
    public ResponseEntity<GenericObject> createObject(@RequestBody GenericObject genericObject, @RequestParam Long citizenId) {
        try {
            // Cria um novo objeto associado ao cidadão
            GenericObject createdObject = genericObjectService.createObject(genericObject, citizenId);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdObject);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<GenericObject> updateObject(@PathVariable Long id, @RequestParam Long citizenId, @RequestBody GenericObject genericObject) {
        try {
            // Atualiza um objeto existente, garantindo que pertence ao cidadão
            GenericObject updatedObject = genericObjectService.updateObject(id, citizenId, genericObject);
            return ResponseEntity.ok(updatedObject);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteObject(@PathVariable Long id, @RequestParam Long citizenId) {
        try {
            // Exclui um objeto associado ao cidadão
            genericObjectService.deleteObject(id, citizenId);
            return ResponseEntity.noContent().build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @GetMapping("/citizen/{citizenId}")
    public ResponseEntity<List<GenericObject>> getObjectsByCitizenId(@PathVariable Long citizenId) {
        try {
            // Retorna os objetos associados a um cidadão específico
            List<GenericObject> objects = genericObjectService.getObjectsByCitizenId(citizenId);
            return ResponseEntity.ok(objects);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}

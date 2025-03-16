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

import br.gov.achei.achei.models.Citizen;
import br.gov.achei.achei.models.GenericObject;
import br.gov.achei.achei.services.CitizenService;
import br.gov.achei.achei.services.GenericObjectService;
import br.gov.achei.achei.utils.JwtUtil;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/objects")
public class GenericObjectController {

    private final GenericObjectService genericObjectService;
    private final CitizenService citizenService;
    private final JwtUtil jwtUtil;

    public GenericObjectController(GenericObjectService genericObjectService, CitizenService citizenService, JwtUtil jwtUtil) {
        this.genericObjectService = genericObjectService;
        this.citizenService = citizenService;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping("/me")
    public ResponseEntity<List<GenericObject>> getAuthenticatedCitizenObjects(@RequestHeader("Authorization") String token) {
        String username = jwtUtil.extractUsername(token.substring(7));
        Citizen citizen = citizenService.getCitizenByUsername(username)
                .orElseThrow(() -> new NoSuchElementException("Citizen not found"));
        return ResponseEntity.ok(genericObjectService.getObjectsByCitizenId(citizen.getId()));
    }

    @GetMapping("/me/{id}")
    public ResponseEntity<GenericObject> getAuthenticatedCitizenObject(@RequestHeader("Authorization") String token, @PathVariable Long id) {
        String username = jwtUtil.extractUsername(token.substring(7));
        Citizen citizen = citizenService.getCitizenByUsername(username)
                .orElseThrow(() -> new NoSuchElementException("Citizen not found"));

        return genericObjectService.getObjectById(id)
                .filter(object -> object.getCitizen().getId().equals(citizen.getId()))
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.FORBIDDEN).build());
    }

    @PostMapping("/me")
    public ResponseEntity<GenericObject> createObject(@RequestHeader("Authorization") String token, @RequestBody GenericObject genericObject) {
        String username = jwtUtil.extractUsername(token.substring(7));
        Citizen citizen = citizenService.getCitizenByUsername(username)
                .orElseThrow(() -> new NoSuchElementException("Citizen not found"));

        GenericObject createdObject = genericObjectService.createObject(genericObject, citizen.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdObject);
    }

    @PutMapping("/me/{id}")
    public ResponseEntity<GenericObject> updateObject(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id,
            @RequestBody GenericObject genericObject) {
        String username = jwtUtil.extractUsername(token.substring(7));
        Citizen citizen = citizenService.getCitizenByUsername(username)
                .orElseThrow(() -> new NoSuchElementException("Citizen not found"));

        try {
            GenericObject updatedObject = genericObjectService.updateObject(id, citizen.getId(), genericObject);
            return ResponseEntity.ok(updatedObject);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @DeleteMapping("/me/{id}")
    public ResponseEntity<Void> deleteObject(@RequestHeader("Authorization") String token, @PathVariable Long id) {
        String username = jwtUtil.extractUsername(token.substring(7));
        Citizen citizen = citizenService.getCitizenByUsername(username)
                .orElseThrow(() -> new NoSuchElementException("Citizen not found"));

        try {
            genericObjectService.deleteObject(id, citizen.getId());
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }
}


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

import br.gov.achei.achei.models.Address;
import br.gov.achei.achei.services.AddressService;
import br.gov.achei.achei.utils.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/addresses")
public class AddressController {

    private final AddressService addressService;
    private final JwtUtil jwtUtil;

    public AddressController(AddressService addressService, JwtUtil jwtUtil) {
        this.addressService = addressService;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping("/me")
    public ResponseEntity<Address> getAuthenticatedCitizenAddress(@RequestHeader("Authorization") String token) {
        String username = jwtUtil.extractUsername(token.substring(7));
        Address address = addressService.getAddressByCitizenUsername(username)
                .orElseThrow(() -> new NoSuchElementException("Address not found"));
        return ResponseEntity.ok(address);
    }

    @PutMapping("/me")
    public ResponseEntity<Address> updateAuthenticatedCitizenAddress(@RequestHeader("Authorization") String token, @RequestBody Address address) {
        String username = jwtUtil.extractUsername(token.substring(7));
        Address updatedAddress = addressService.updateAddressByCitizenUsername(username, address);
        return ResponseEntity.ok(updatedAddress);
    }
}

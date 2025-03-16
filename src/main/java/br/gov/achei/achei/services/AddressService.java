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

package br.gov.achei.achei.services;

import br.gov.achei.achei.models.Address;
import br.gov.achei.achei.models.Citizen;
import br.gov.achei.achei.repositories.AddressRepository;
import br.gov.achei.achei.repositories.CitizenRepository;
import br.gov.achei.achei.utils.EncryptionUtil;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class AddressService {

    private final AddressRepository addressRepository;
    private final CitizenRepository citizenRepository;

    public AddressService(AddressRepository addressRepository, CitizenRepository citizenRepository) {
        this.addressRepository = addressRepository;
        this.citizenRepository = citizenRepository;
    }

    public Optional<Address> getAddressByCitizenUsername(String username) {
        return citizenRepository.findByUserUsername(EncryptionUtil.encrypt(username))
                .map(Citizen::getAddress);
    }

    public Address updateAddressByCitizenUsername(String username, Address newAddress) {
        return citizenRepository.findByUserUsername(EncryptionUtil.encrypt(username)).map(citizen -> {
            Address currentAddress = citizen.getAddress();
            if (currentAddress == null) {
                throw new NoSuchElementException("Address not found for the citizen");
            }
            BeanUtils.copyProperties(newAddress, currentAddress, "id", "createdAt", "citizen");
            currentAddress.setUpdatedAt(newAddress.getUpdatedAt());
            return addressRepository.save(currentAddress);
        }).orElseThrow(() -> new NoSuchElementException("Citizen not found"));
    }
}

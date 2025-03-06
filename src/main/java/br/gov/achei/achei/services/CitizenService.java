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
// import br.gov.achei.achei.models.GenericObject;
import br.gov.achei.achei.repositories.AddressRepository;
import br.gov.achei.achei.repositories.CitizenRepository;
import br.gov.achei.achei.repositories.GenericObjectRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.NoSuchElementException;

@Service
public class CitizenService {

    private final CitizenRepository citizenRepository;
    private final AddressRepository addressRepository;
    private final GenericObjectRepository genericObjectRepository;

    public CitizenService(CitizenRepository citizenRepository, AddressRepository addressRepository, GenericObjectRepository genericObjectRepository) {
        this.citizenRepository = citizenRepository;
        this.addressRepository = addressRepository;
        this.genericObjectRepository = genericObjectRepository;
    }

    public List<Citizen> getAllCitizens() {
        List<Citizen> citizens = citizenRepository.findAll();
        citizens.forEach(Citizen::decryptDataAfterLoad);
        return citizens;
    }
    
    public Optional<Citizen> getCitizenById(Long id) {
        Optional<Citizen> citizen = citizenRepository.findById(id);
        citizen.ifPresent(Citizen::decryptDataAfterLoad);
        return citizen;
    }    

    public Citizen createCitizen(Citizen citizen) {
        if (citizen.getAddress() == null) {
            throw new IllegalArgumentException("Address cannot be null");
        }

        citizen.encryptDataBeforePersist();
        Address address = citizen.getAddress();
        address.setCitizen(citizen);
        citizen.setAddress(address);

        Citizen savedCitizen = citizenRepository.save(citizen);
        savedCitizen.decryptDataAfterLoad();
        return savedCitizen;
    }

    public Citizen updateCitizen(Long id, Citizen citizen) {
        return citizenRepository.findById(id).map(existingCitizen -> {
            BeanUtils.copyProperties(citizen, existingCitizen, "id", "createdAt", "updatedAt", "password", "address", "objects");

            if (citizen.getPassword() != null && !citizen.getPassword().isEmpty()) {
                existingCitizen.setPassword(citizen.getPassword());
            }

            existingCitizen.encryptDataBeforeUpdate();
            citizenRepository.save(existingCitizen);
            existingCitizen.decryptDataAfterLoad();

            if (citizen.getAddress() != null) {
                Address existingAddress = existingCitizen.getAddress();
                Address newAddress = citizen.getAddress();
                BeanUtils.copyProperties(newAddress, existingAddress, "id", "createdAt", "updatedAt", "citizen");
                addressRepository.save(existingAddress);
            }

            return existingCitizen;
        }).orElseThrow(NoSuchElementException::new);
    }

    public void deleteCitizen(Long id) {
        Citizen citizen = citizenRepository.findById(id).orElseThrow(NoSuchElementException::new);
        genericObjectRepository.deleteAll(citizen.getObjects());
        citizenRepository.deleteById(id);
    }
}

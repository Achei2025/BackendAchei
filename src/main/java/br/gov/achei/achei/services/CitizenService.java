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
import br.gov.achei.achei.models.Comment;
import br.gov.achei.achei.models.User;
import br.gov.achei.achei.repositories.AddressRepository;
import br.gov.achei.achei.repositories.CitizenRepository;
import br.gov.achei.achei.repositories.GenericObjectRepository;
import br.gov.achei.achei.repositories.UserRepository;
import br.gov.achei.achei.utils.AnonymousNameGenerator;
import br.gov.achei.achei.utils.EncryptionUtil;

import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.NoSuchElementException;

@Service
public class CitizenService {

    private final CitizenRepository citizenRepository;
    private final AddressRepository addressRepository;
    private final GenericObjectRepository genericObjectRepository;
    private final UserService userService;

    public CitizenService(CitizenRepository citizenRepository, 
                          AddressRepository addressRepository, 
                          GenericObjectRepository genericObjectRepository,
                          UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          UserService userService) {
        this.citizenRepository = citizenRepository;
        this.addressRepository = addressRepository;
        this.genericObjectRepository = genericObjectRepository;
        this.userService = userService;
    }

    public List<Citizen> getAllCitizens() {
        List<Citizen> citizens = citizenRepository.findAll();
        citizens.forEach(Citizen::decryptDataAfterLoad);
        return citizens;
    }

    public Optional<Citizen> getCitizenByUsername(String username) {
        return citizenRepository.findByUserUsername(EncryptionUtil.encrypt(username));
    }

    public Citizen createCitizen(Citizen citizen, String rawPassword) {
        if (citizenRepository.existsByCpf(citizen.getCpf())) {
            throw new IllegalArgumentException("A citizen with the provided CPF already exists.");
        }
    
        if (citizen.getFullName() == null || citizen.getFullName().isEmpty()) {
            throw new IllegalArgumentException("The full name is required.");
        }
    
        String anonymousName;
        do {
            anonymousName = AnonymousNameGenerator.generateAnonymousName();
        } while (citizenRepository.findByAnonymousName(EncryptionUtil.encrypt(anonymousName)).isPresent());
        citizen.setAnonymousName(anonymousName);
    
        citizen.encryptDataBeforePersist();
        
        User user = userService.registerUser(citizen.getCpf(), rawPassword);
        citizen.setUser(user);
    
        if (citizen.getAddress() != null) {
            citizen.getAddress().setCitizen(citizen);
        }
    
        Citizen savedCitizen = citizenRepository.save(citizen);
        savedCitizen.decryptDataAfterLoad();
        return savedCitizen;
    }
    
    public Citizen updateCitizen(Long id, Citizen citizen) {
        return citizenRepository.findById(id).map(existingCitizen -> {
            if (citizen.getFullName() == null || citizen.getFullName().isEmpty()) {
                throw new IllegalArgumentException("The full name cannot be empty.");
            }

            List<Comment> existingComments = existingCitizen.getComments();
            citizen.setComments(existingComments);
    
            BeanUtils.copyProperties(citizen, existingCitizen, "id", "createdAt", "updatedAt", "user", "address", "objects", "cases");
    
            if (citizen.getAddress() != null) {
                Address existingAddress = existingCitizen.getAddress();
                Address newAddress = citizen.getAddress();
                BeanUtils.copyProperties(newAddress, existingAddress, "id", "createdAt", "updatedAt", "citizen");
                addressRepository.save(existingAddress);
            }
    
            existingCitizen.encryptDataBeforeUpdate();
            citizenRepository.save(existingCitizen);
    
            existingCitizen.decryptDataAfterLoad();
            return existingCitizen;
        }).orElseThrow(() -> new NoSuchElementException("Citizen with the specified ID not found."));
    }
    
    public void deleteCitizen(Long id) {
        Citizen citizen = citizenRepository.findById(id).orElseThrow(() -> 
            new NoSuchElementException("Citizen with the specified ID not found."));
        genericObjectRepository.deleteAll(citizen.getObjects());
        citizenRepository.deleteById(id);
    }
}

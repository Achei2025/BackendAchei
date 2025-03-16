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

import br.gov.achei.achei.models.Case;
import br.gov.achei.achei.models.Citizen;
import br.gov.achei.achei.models.GenericObject;
import br.gov.achei.achei.models.Police;
import br.gov.achei.achei.repositories.CaseRepository;
import br.gov.achei.achei.repositories.CitizenRepository;
import br.gov.achei.achei.repositories.GenericObjectRepository;
import br.gov.achei.achei.repositories.PoliceRepository;
import br.gov.achei.achei.utils.EncryptionUtil;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class CaseService {

    private final CaseRepository caseRepository;
    private final CitizenRepository citizenRepository;
    private final PoliceRepository policeRepository;
    private final GenericObjectRepository genericObjectRepository;

    public CaseService(CaseRepository caseRepository, CitizenRepository citizenRepository, PoliceRepository policeRepository, GenericObjectRepository genericObjectRepository) {
        this.caseRepository = caseRepository;
        this.citizenRepository = citizenRepository;
        this.policeRepository = policeRepository;
        this.genericObjectRepository = genericObjectRepository;
    }

    public List<Case> getAllCases() {
        return caseRepository.findAll().stream().map(caseItem -> {
            if (EncryptionUtil.isEncrypted(caseItem.getCitizen().getAnonymousName())) {
                caseItem.getCitizen().decryptDataAfterLoad();
            }

            Case filteredCase = new Case();
            filteredCase.setId(caseItem.getId());
            filteredCase.setCrimeType(caseItem.getCrimeType());
            filteredCase.setReportedAt(caseItem.getReportedAt());
            filteredCase.setLocation(caseItem.getLocation());
            filteredCase.setDescription(caseItem.getDescription());
            filteredCase.setMessages(caseItem.getMessages());
            filteredCase.setComments(caseItem.getComments());

            Citizen citizen = new Citizen();
            citizen.setAnonymousName(caseItem.getCitizen().getAnonymousName());
            filteredCase.setCitizen(citizen);

            if (caseItem.getPolice() != null) {
                Police police = new Police();
                police.setName(EncryptionUtil.decrypt(caseItem.getPolice().getName()));
                filteredCase.setPolice(police);
            }

            if (caseItem.getObject() != null) {
                GenericObject filteredObject = new GenericObject();
                if (caseItem.getObject().getName() != null) filteredObject.setName(caseItem.getObject().getName());
                if (caseItem.getObject().getCategory() != null) filteredObject.setCategory(caseItem.getObject().getCategory());
                if (caseItem.getObject().getDescription() != null) filteredObject.setDescription(caseItem.getObject().getDescription());
                if (caseItem.getObject().getBrand() != null) filteredObject.setBrand(caseItem.getObject().getBrand());
                if (caseItem.getObject().getModel() != null) filteredObject.setModel(caseItem.getObject().getModel());
                if (caseItem.getObject().getImage() != null) filteredObject.setImage(caseItem.getObject().getImage());
                filteredCase.setObject(filteredObject);
            }

            return filteredCase;
        }).collect(Collectors.toList());
    }

    public Case getCaseById(Long id) {
        Case existingCase = caseRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Case not found"));
    
        Citizen citizen = existingCase.getCitizen();
        if (citizen != null && EncryptionUtil.isEncrypted(citizen.getAnonymousName())) {
            citizen.decryptDataAfterLoad();
        }
    
        Case filteredCase = new Case();
        filteredCase.setId(existingCase.getId());
        filteredCase.setCrimeType(existingCase.getCrimeType());
        filteredCase.setReportedAt(existingCase.getReportedAt());
        filteredCase.setLocation(existingCase.getLocation());
        filteredCase.setDescription(existingCase.getDescription());
        filteredCase.setMessages(existingCase.getMessages());
        filteredCase.setComments(existingCase.getComments());
    
        if (citizen != null) {
            Citizen filteredCitizen = new Citizen();
            filteredCitizen.setAnonymousName(citizen.getAnonymousName());
            filteredCase.setCitizen(filteredCitizen);
        }
    
        if (existingCase.getPolice() != null) {
            Police police = new Police();
            police.setName(EncryptionUtil.decrypt(existingCase.getPolice().getName()));
            filteredCase.setPolice(police);
        }
    
        if (existingCase.getObject() != null) {
            GenericObject filteredObject = new GenericObject();
            if (existingCase.getObject().getName() != null) filteredObject.setName(existingCase.getObject().getName());
            if (existingCase.getObject().getCategory() != null) filteredObject.setCategory(existingCase.getObject().getCategory());
            if (existingCase.getObject().getDescription() != null) filteredObject.setDescription(existingCase.getObject().getDescription());
            if (existingCase.getObject().getBrand() != null) filteredObject.setBrand(existingCase.getObject().getBrand());
            if (existingCase.getObject().getModel() != null) filteredObject.setModel(existingCase.getObject().getModel());
            if (existingCase.getObject().getImage() != null) filteredObject.setImage(existingCase.getObject().getImage());
            filteredCase.setObject(filteredObject);
        }
    
        return filteredCase;
    }

    public Case createCase(Case caseData, Long citizenId, Long objectId) {
        Citizen citizen = citizenRepository.findById(citizenId)
                .orElseThrow(() -> new NoSuchElementException("Citizen not found"));

        GenericObject object = genericObjectRepository.findById(objectId)
                .orElseThrow(() -> new NoSuchElementException("Object not found"));

        if (!object.getCitizen().getId().equals(citizenId)) {
            throw new IllegalArgumentException("The object does not belong to the specified citizen.");
        }

        caseData.setCitizen(citizen);
        caseData.setObject(object);

        return caseRepository.save(caseData);
    }

    public Case updateCase(Long id, Long citizenId, Case updatedCase, Long policeId, Long objectId) {
        return caseRepository.findById(id).map(existingCase -> {
            if (!existingCase.getCitizen().getId().equals(citizenId)) {
                throw new IllegalArgumentException("Case does not belong to the citizen.");
            }
            existingCase.setStatus(updatedCase.getStatus());
            existingCase.setCrimeType(updatedCase.getCrimeType());
            existingCase.setLocation(updatedCase.getLocation());
            existingCase.setDescription(updatedCase.getDescription());

            if (policeId != null) {
                Police police = policeRepository.findById(policeId)
                        .orElseThrow(() -> new NoSuchElementException("Police not found"));
                existingCase.setPolice(police);
            }

            if (objectId != null) {
                GenericObject object = genericObjectRepository.findById(objectId)
                        .orElseThrow(() -> new NoSuchElementException("Object not found"));
                existingCase.setObject(object);
            }
            return caseRepository.save(existingCase);
        }).orElseThrow(() -> new NoSuchElementException("Case not found"));
    }

    public void deleteCase(Long id, Long citizenId) {
        caseRepository.findById(id).ifPresent(existingCase -> {
            if (!existingCase.getCitizen().getId().equals(citizenId)) {
                throw new IllegalArgumentException("Case does not belong to the citizen.");
            }
            caseRepository.delete(existingCase);
        });
    }
}

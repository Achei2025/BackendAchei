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

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

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
        List<Case> cases = caseRepository.findAll();
        cases.forEach(caseItem -> {
            if (caseItem.getPolice() != null) {
                caseItem.getPolice().decryptDataAfterLoad();
            }
        });
        return cases;
    }    

    public Case getCaseById(Long id) {
        Case existingCase = caseRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Case not found"));
    
        if (existingCase.getPolice() != null) {
            existingCase.getPolice().decryptDataAfterLoad();
        }
    
        if (existingCase.getObject() != null) {
            existingCase.getObject().getName();
        }
    
        return existingCase;
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

    public Case updateCase(Long id, Case updatedCase, Long policeId, Long objectId) {
        return caseRepository.findById(id).map(existingCase -> {
            existingCase.setStatus(updatedCase.getStatus());
            existingCase.setCrimeType(updatedCase.getCrimeType());
            existingCase.setLocation(updatedCase.getLocation());
            existingCase.setDescription(updatedCase.getDescription());
            existingCase.setPriority(updatedCase.getPriority());
    
            if (policeId != null) {
                Police police = policeRepository.findById(policeId)
                        .orElseThrow(() -> new NoSuchElementException("Police not found"));
                existingCase.setPolice(police);

                police.decryptDataAfterLoad();
            }
    
            if (objectId != null) {
                GenericObject object = genericObjectRepository.findById(objectId)
                        .orElseThrow(() -> new NoSuchElementException("Object not found"));
                existingCase.setObject(object);
            }
    
            return caseRepository.save(existingCase);
        }).orElseThrow(() -> new NoSuchElementException("Case not found"));
    }    
    
    public void deleteCase(Long id) {
        caseRepository.deleteById(id);
    }
}

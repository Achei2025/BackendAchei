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

import br.gov.achei.achei.models.Citizen;
import br.gov.achei.achei.models.GenericObject;
import br.gov.achei.achei.models.ObjectProperty;
import br.gov.achei.achei.repositories.CitizenRepository;
import br.gov.achei.achei.repositories.GenericObjectRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class GenericObjectService {

    private final GenericObjectRepository genericObjectRepository;
    private final CitizenRepository citizenRepository;

    public GenericObjectService(GenericObjectRepository genericObjectRepository, CitizenRepository citizenRepository) {
        this.genericObjectRepository = genericObjectRepository;
        this.citizenRepository = citizenRepository;
    }

    public List<GenericObject> getAllObjects() {
        return genericObjectRepository.findAll();
    }

    public Optional<GenericObject> getObjectById(Long id) {
        return genericObjectRepository.findById(id);
    }

    public GenericObject createObject(GenericObject genericObject, Long citizenId) {
        Citizen citizen = citizenRepository.findById(citizenId)
                .orElseThrow(() -> new NoSuchElementException("Citizen not found"));
        genericObject.setCitizen(citizen);

        if (genericObject.getProperties() != null) {
            genericObject.getProperties().forEach(property -> property.setObject(genericObject));
        }

        return genericObjectRepository.save(genericObject);
    }

    public GenericObject updateObject(Long objectId, Long citizenId, GenericObject updatedObject) {
        return genericObjectRepository.findById(objectId).map(existingObject -> {
            if (!existingObject.getCitizen().getId().equals(citizenId)) {
                throw new IllegalArgumentException("Object does not belong to the citizen.");
            }

        Map<Long, ObjectProperty> existingPropertiesMap = existingObject.getProperties().stream()
            .collect(Collectors.toMap(ObjectProperty::getId, property -> property));

        List<ObjectProperty> updatedProperties = new ArrayList<>();
        for (ObjectProperty updatedProperty : updatedObject.getProperties()) {
            if (updatedProperty.getId() != null) {
                ObjectProperty existingProperty = existingPropertiesMap.get(updatedProperty.getId());
                if (existingProperty != null) {
                    existingProperty.setKey(updatedProperty.getKey());
                    existingProperty.setValue(updatedProperty.getValue());
                    existingProperty.setBlock(updatedProperty.isBlock());
                    updatedProperties.add(existingProperty);
                }
            } else {
                updatedProperty.setObject(existingObject);
                updatedProperties.add(updatedProperty);
            }
        }

        existingObject.getProperties().clear();

        existingObject.getProperties().addAll(updatedProperties);

        BeanUtils.copyProperties(updatedObject, existingObject, "id", "createdAt", "citizen", "properties");

            return genericObjectRepository.save(existingObject);
        }).orElseThrow(() -> new NoSuchElementException("Object not found"));
    }
               
    public void deleteObject(Long objectId, Long citizenId) {
        GenericObject genericObject = genericObjectRepository.findById(objectId)
                .orElseThrow(() -> new NoSuchElementException("Object not found"));
        if (!genericObject.getCitizen().getId().equals(citizenId)) {
            throw new IllegalArgumentException("Object does not belong to the citizen.");
        }

        genericObjectRepository.delete(genericObject);
    }

    public List<GenericObject> getObjectsByCitizenId(Long citizenId) {
        Citizen citizen = citizenRepository.findById(citizenId)
                .orElseThrow(() -> new NoSuchElementException("Citizen not found"));
        return citizen.getObjects();
    }
}

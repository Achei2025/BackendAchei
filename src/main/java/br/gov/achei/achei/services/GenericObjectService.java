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
// import java.util.ArrayList;
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
        // Retorna todos os objetos (se necessário, incluir filtros)
        return genericObjectRepository.findAll();
    }

    public Optional<GenericObject> getObjectById(Long id) {
        // Busca um objeto pelo ID
        return genericObjectRepository.findById(id);
    }

    public GenericObject createObject(GenericObject genericObject, Long citizenId) {
        // Verifica se o cidadão existe e associa o objeto
        Citizen citizen = citizenRepository.findById(citizenId)
                .orElseThrow(() -> new NoSuchElementException("Citizen not found"));
        genericObject.setCitizen(citizen);

        // Associa as propriedades ao objeto
        if (genericObject.getProperties() != null) {
            genericObject.getProperties().forEach(property -> property.setObject(genericObject));
        }

        // Salva o objeto no repositório
        return genericObjectRepository.save(genericObject);
    }

    public GenericObject updateObject(Long objectId, Long citizenId, GenericObject updatedObject) {
        return genericObjectRepository.findById(objectId).map(existingObject -> {
            if (!existingObject.getCitizen().getId().equals(citizenId)) {
                throw new IllegalArgumentException("Object does not belong to the citizen.");
            }

        // Criar um mapa para rastrear as propriedades existentes
        Map<Long, ObjectProperty> existingPropertiesMap = existingObject.getProperties().stream()
            .collect(Collectors.toMap(ObjectProperty::getId, property -> property));

        // Atualizar ou adicionar propriedades
        List<ObjectProperty> updatedProperties = new ArrayList<>();
        for (ObjectProperty updatedProperty : updatedObject.getProperties()) {
            if (updatedProperty.getId() != null) {
                // Atualiza propriedades existentes
                ObjectProperty existingProperty = existingPropertiesMap.get(updatedProperty.getId());
                if (existingProperty != null) {
                    existingProperty.setKey(updatedProperty.getKey());
                    existingProperty.setValue(updatedProperty.getValue());
                    updatedProperties.add(existingProperty);
                }
            } else {
                // Adiciona novas propriedades
                updatedProperty.setObject(existingObject);
                updatedProperties.add(updatedProperty);
            }
        }

        // Limpa a lista antiga para evitar inconsistências
        existingObject.getProperties().clear();

        // Adiciona as propriedades atualizadas à entidade
        existingObject.getProperties().addAll(updatedProperties);

        // Atualiza os demais atributos da entidade
        BeanUtils.copyProperties(updatedObject, existingObject, "id", "createdAt", "citizen", "properties");

            // Salva e retorna o objeto atualizado
            return genericObjectRepository.save(existingObject);
        }).orElseThrow(() -> new NoSuchElementException("Object not found"));
    }
    
               
    public void deleteObject(Long objectId, Long citizenId) {
        // Verifica se o objeto pertence ao cidadão antes de excluí-lo
        GenericObject genericObject = genericObjectRepository.findById(objectId)
                .orElseThrow(() -> new NoSuchElementException("Object not found"));
        if (!genericObject.getCitizen().getId().equals(citizenId)) {
            throw new IllegalArgumentException("Object does not belong to the citizen.");
        }

        // Exclui o objeto
        genericObjectRepository.delete(genericObject);
    }

    public List<GenericObject> getObjectsByCitizenId(Long citizenId) {
        // Retorna todos os objetos associados a um cidadão
        Citizen citizen = citizenRepository.findById(citizenId)
                .orElseThrow(() -> new NoSuchElementException("Citizen not found"));
        return citizen.getObjects();
    }
}

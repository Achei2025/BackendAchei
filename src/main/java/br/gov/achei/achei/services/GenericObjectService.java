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

            // Criar um mapa para atualizar propriedades existentes
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

            // Define as propriedades atualizadas no objeto
            existingObject.setProperties(updatedProperties);

            // Atualiza outros atributos do objeto, exceto os imutáveis e propriedades
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

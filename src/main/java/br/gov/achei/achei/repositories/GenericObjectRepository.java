package br.gov.achei.achei.repositories;

import br.gov.achei.achei.models.GenericObject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GenericObjectRepository extends JpaRepository<GenericObject, Long> {

    Optional<GenericObject> findByIdentificationCode(String identificationCode);

    boolean existsByIdentificationCode(String identificationCode);

    Optional<GenericObject> findByName(String name);

    boolean existsByName(String name);
}

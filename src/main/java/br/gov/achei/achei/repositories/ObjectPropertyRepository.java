package br.gov.achei.achei.repositories;

import br.gov.achei.achei.models.ObjectProperty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ObjectPropertyRepository extends JpaRepository<ObjectProperty, Long> {
}

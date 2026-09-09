package com.formation.classservice.repository;

import com.formation.classservice.model.FitnessClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * JpaSpecificationExecutor permet de composer des criteres de recherche dynamiques
 * (category, level, location, instructor, dateFrom/dateTo) sans ecrire une methode
 * derivee par combinaison de filtres : voir ClassSpecifications.
 */
public interface ClassRepository extends JpaRepository<FitnessClass, Long>, JpaSpecificationExecutor<FitnessClass> {
}

package com.formation.classservice.repository;

import com.formation.classservice.dto.ClassSearchCriteria;
import com.formation.classservice.model.FitnessClass;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

/**
 * Une Specification<T> est un objet qui sait construire un predicat JPA Criteria
 * (bout de clause WHERE) a partir de l'entite. Specification.where(null) est un
 * predicat neutre (equivalent a "1=1"), et .and(...) les combine : ainsi, seuls
 * les criteres realisent non nuls dans ClassSearchCriteria ajoutent une condition,
 * le tout formant une seule requete SQL generee dynamiquement par Hibernate.
 */
public final class ClassSpecifications {

    private ClassSpecifications() {
    }

    public static Specification<FitnessClass> fromCriteria(ClassSearchCriteria criteria) {
        return Specification.where(hasCategory(criteria.getCategory()))
                .and(hasLevel(criteria.getLevel()))
                .and(hasLocation(criteria.getLocation()))
                .and(hasInstructor(criteria.getInstructor()))
                .and(dateFrom(criteria.getDateFrom()))
                .and(dateTo(criteria.getDateTo()));
    }

    private static Specification<FitnessClass> hasCategory(com.formation.classservice.model.ClassCategory category) {
        return (root, query, cb) -> category == null ? null : cb.equal(root.get("category"), category);
    }

    private static Specification<FitnessClass> hasLevel(com.formation.classservice.model.ClassLevel level) {
        return (root, query, cb) -> level == null ? null : cb.equal(root.get("level"), level);
    }

    private static Specification<FitnessClass> hasLocation(String location) {
        return (root, query, cb) -> (location == null || location.isBlank())
                ? null
                : cb.like(cb.lower(root.get("gymLocation")), "%" + location.toLowerCase() + "%");
    }

    private static Specification<FitnessClass> hasInstructor(String instructor) {
        return (root, query, cb) -> (instructor == null || instructor.isBlank())
                ? null
                : cb.like(cb.lower(root.get("instructor")), "%" + instructor.toLowerCase() + "%");
    }

    private static Specification<FitnessClass> dateFrom(java.time.LocalDate dateFrom) {
        return (root, query, cb) -> dateFrom == null
                ? null
                : cb.greaterThanOrEqualTo(root.get("dateTime"), dateFrom.atStartOfDay());
    }

    private static Specification<FitnessClass> dateTo(java.time.LocalDate dateTo) {
        return (root, query, cb) -> dateTo == null
                ? null
                : cb.lessThan(root.get("dateTime"), (LocalDateTime) dateTo.plusDays(1).atStartOfDay());
    }
}

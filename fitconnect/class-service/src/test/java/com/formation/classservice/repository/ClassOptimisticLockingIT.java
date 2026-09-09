package com.formation.classservice.repository;

import com.formation.classservice.model.ClassCategory;
import com.formation.classservice.model.ClassLevel;
import com.formation.classservice.model.FitnessClass;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Verifie le VRAI comportement d'Hibernate face au verrouillage optimiste (pas un mock) :
 * deux instances distinctes de la meme ligne, chacune modifiee puis sauvegardee -> la seconde
 * sauvegarde doit echouer. C'est exactement le scenario "deux etudiants reservent la derniere
 * place en meme temps" decrit dans le sujet.
 *
 * Important : on N'utilise PAS @Transactional sur cette classe de test. Une seule transaction
 * partagerait le meme persistence context (donc le meme cache de 1er niveau), et findById()
 * renverrait alors DEUX FOIS LE MEME OBJET JAVA au lieu de deux copies independantes - le
 * conflit de version ne se produirait jamais.
 */
@SpringBootTest
class ClassOptimisticLockingIT {

    @Autowired
    private ClassRepository classRepository;

    @Test
    void deuxLecturesConcurrentes_laSecondeSauvegardeEchoueAvecOptimisticLockException() {
        FitnessClass created = new FitnessClass("Spinning", "Cardio intense", "Yasmine", "Marseille",
                ClassCategory.SPINNING, ClassLevel.INTERMEDIATE, 45, 10,
                new BigDecimal("15.00"), LocalDateTime.now().plusDays(3));
        created.setCurrentParticipants(9); // il ne reste qu'UNE place
        Long id = classRepository.saveAndFlush(created).getId();

        // Deux appels findById() SEPARES (hors transaction) = deux objets Java independants,
        // tous les deux avec version=0, comme si deux requetes HTTP concurrentes lisaient l'etat.
        FitnessClass readByUserA = classRepository.findById(id).orElseThrow();
        FitnessClass readByUserB = classRepository.findById(id).orElseThrow();

        readByUserA.reserveSpots(1); // 9 -> 10, OK cote logique metier
        classRepository.saveAndFlush(readByUserA); // UPDATE ... WHERE id=? AND version=0 -> OK, version passe a 1

        readByUserB.reserveSpots(1); // toujours 9 -> 10 du point de vue de B (donnees perimees)

        // UPDATE ... WHERE id=? AND version=0 -> 0 ligne affectee (la version reelle est 1) ->
        // Hibernate detecte l'ecart et leve ObjectOptimisticLockingFailureException.
        assertThatThrownBy(() -> classRepository.saveAndFlush(readByUserB))
                .isInstanceOf(ObjectOptimisticLockingFailureException.class);

        // L'etat final en base reflete UNIQUEMENT la premiere ecriture reussie : 10, pas 11.
        FitnessClass finalState = classRepository.findById(id).orElseThrow();
        assertThat(finalState.getCurrentParticipants()).isEqualTo(10);
        assertThat(finalState.getVersion()).isEqualTo(1L);
    }
}

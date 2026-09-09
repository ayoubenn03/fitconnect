# FitConnect — Réservation & paiement de cours de sport

TP microservices EFRIE M2-DEV1 : 4 nouveaux services métier (`class-service`, `booking-service`,
`payment-service`, `notification-service`) ajoutés à l'infrastructure existante
(`eureka-server`, `config-server`, `api-gateway`, `product-service`, `order-service`).

Page de suivi pédagogique (architecture, avancement, explication de chaque concept avec le vrai
code) : voir l'artifact publié pendant la session — elle reste la meilleure porte d'entrée pour
comprendre le "pourquoi" derrière chaque choix technique avant de lire ce README.

## Architecture

```
Client / Postman
      │
      ▼
api-gateway (:8080)  ──────────────────────────────────────────┐
      │  /api/classes/**   /api/bookings/**  /api/payments/**  │  /api/notifications/**
      ▼                          ▼                  ▼          ▼
class-service (:8091)   booking-service (:8092)  payment-service (:8093)  notification-service (:8094)
                                 │  Feign #1: verifie/reserve  ─┴─▶ class-service
                                 │  Feign #2: encaisse/rembourse ─▶ payment-service
                                 │  Feign #3: notifie ────────────▶ notification-service
                                 │
                     (orchestrateur de la Saga — aucune transaction distribuée,
                      compensation explicite en cas d'échec intermédiaire)

Tous les services s'enregistrent auprès d'eureka-server (:8761) et lisent leur
configuration depuis config-server (:8888, profil natif, fichiers dans config-server/config-repo/).
```

`booking-service` est le seul service à appeler les trois autres : c'est l'orchestrateur de la
Saga. `class-service`, `payment-service` et `notification-service` ne se connaissent pas entre eux.

## Le pattern Saga (orchestré)

Une réservation traverse jusqu'à 4 étapes locales, chacune dans un service différent, sans
transaction distribuée qui engloberait les 4 bases :

1. **Vérifier le cours** (`GET /api/classes/{id}` sur class-service) et capturer un *snapshot*
   (nom, date, instructeur, prix) dans la réservation — ce snapshot ne change plus jamais
   rétroactivement, même si le cours est modifié ensuite.
2. **Réserver les places** (`PATCH /api/classes/{id}/increment`) — protégé par verrouillage
   optimiste (`@Version`) côté class-service : un conflit renvoie 409, traduit en
   `NoSpotsAvailableException` côté booking-service.
3. **Créer la réservation** (statut `PENDING_PAYMENT`, deadline de paiement = +1h). Si cette étape
   échoue *après* que les places ont été prises à l'étape 2, booking-service **compense** en
   appelant `PATCH /api/classes/{id}/decrement` pour les relâcher.
4. **Notifier** (best-effort — un échec d'envoi ne bloque et n'annule jamais la réservation).

Le paiement est un flux séparé (`PATCH /api/bookings/{id}/confirm`), déclenché par le client une
fois la réservation créée. L'annulation (`PATCH /api/bookings/{id}/cancel`) rembourse uniquement si
la réservation était `CONFIRMED`, mais relâche toujours les places, qu'elle ait été payée ou non.

Deux tâches planifiées (`@Scheduled`, cron externalisés dans `config-repo/booking-service.yml`)
tournent dans booking-service : expiration des réservations impayées après 1h (toutes les 5 min),
et rappel de cours 24h avant (toutes les heures, fenêtre glissante `[+24h, +25h)`).

## Prérequis

- JDK 17
- Maven 3.9+
- Docker + Docker Compose (pour le lancement conteneurisé)

## Lancer le projet

### Option A — Docker Compose (recommandé)

```bash
docker compose up --build
```

Démarre les 9 services dans le bon ordre (`depends_on` + healthchecks) : eureka-server et
config-server d'abord, puis les services métier, puis api-gateway en dernier. Premier démarrage
plus long (téléchargement des images Maven/JDK + build de chaque module).

| Service               | Port | URL locale                          |
|------------------------|------|--------------------------------------|
| eureka-server          | 8761 | http://localhost:8761                |
| config-server          | 8888 | http://localhost:8888                |
| api-gateway             | 8080 | http://localhost:8080                |
| product-service         | 8081 | http://localhost:8081                |
| order-service            | 8082 | http://localhost:8082                |
| class-service            | 8091 | http://localhost:8091/swagger-ui.html |
| booking-service           | 8092 | http://localhost:8092/swagger-ui.html |
| payment-service            | 8093 | http://localhost:8093/swagger-ui.html |
| notification-service        | 8094 | http://localhost:8094/swagger-ui.html |

Toutes les requêtes métier passent par la gateway : `http://localhost:8080/api/...`.

### Option B — En local, service par service

```bash
# Dans des terminaux séparés, dans cet ordre :
mvn -pl eureka-server -am spring-boot:run
mvn -pl config-server -am spring-boot:run
mvn -pl class-service -am spring-boot:run
mvn -pl payment-service -am spring-boot:run
mvn -pl notification-service -am spring-boot:run
mvn -pl booking-service -am spring-boot:run
mvn -pl api-gateway -am spring-boot:run
```

## Tests

```bash
mvn -pl class-service,booking-service,payment-service,notification-service -am test
```

- Tests unitaires (`*Test.java`) : `@ExtendWith(MockitoExtension.class)`, dépendances mockées
  (repository + clients Feign côté booking-service), aucune base ni réseau réel.
- Tests d'intégration (`*IntegrationTest.java`, `*IT.java`) : `@SpringBootTest` avec H2 en mémoire ;
  les appels sortants de booking-service vers les 3 autres services sont mockés (`@MockBean`) pour
  isoler le service testé.
- `ClassOptimisticLockingIT` est le seul test qui provoque un vrai
  `ObjectOptimisticLockingFailureException` (deux lectures indépendantes, volontairement sans
  `@Transactional` sur la méthode de test).

## Collection Postman

`postman/FitConnect.postman_collection.json` — à importer dans Postman. Variable de collection
`gatewayUrl` (défaut `http://localhost:8080`). Organisée en dossiers :

- **01 - Class management** : CRUD + recherche/filtres + pagination
- **02 - Booking - parcours complet** : création → confirmation paiement → completion (chaîne les
  ids automatiquement via des scripts de test Postman)
- **03 - Booking - places épuisées** : 2 réservations sur un cours à 1 place → la 2e renvoie 409
- **04 - Booking - annulation avec remboursement**
- **05 - Payment (accès direct)** : paiement accepté / refusé (seuil), remboursement
- **06 - Notification (accès direct)** : envoi, historique, pending, retry (« pending » = `FAILED` en attente de retry)
- **07 - Scénarios d'erreur** : 404 sur les 4 services, erreurs de validation (durée de cours
  invalide, email invalide, format de carte invalide)

## Config centralisée

Chaque service lit sa config depuis `config-server/config-repo/<nom-du-service>.yml` (profil
`native`, pas de repo Git). Ports, timeouts Feign, configuration Resilience4j (dont
`ignore-exceptions` pour ne pas compter les 404/409/400 métier comme des pannes du Circuit
Breaker) et expressions cron des schedulers y sont externalisés — pas de valeur métier codée en
dur dans le Java.

## Statuts d'une réservation

```
PENDING_PAYMENT ──confirm (succès)──▶ CONFIRMED ──complete──▶ COMPLETED
      │                                    │
      │ cancel / expiration (>1h)          │ cancel (avant cancellationDeadline = classDate-24h)
      ▼                                    ▼
  CANCELLED  ◀────────────────────── CANCELLED (+ remboursement)
```

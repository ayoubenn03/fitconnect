# config-server

Serveur de configuration centralisee (Spring Cloud Config, profil **native**). Les fichiers de configuration de chaque microservice sont stockes dans `config-repo/` (au lieu d'un depot Git distant, pour simplifier l'usage en formation).

## Port

`8888`

## Demarrage

```bash
mvn spring-boot:run
```

**Important** : la commande doit etre lancee depuis le dossier `config-server/` (ou via `mvn -pl config-server spring-boot:run` depuis la racine), car le chemin `file:./config-repo` est relatif au repertoire de travail.

## Structure de `config-repo/`

| Fichier | Portee |
|---|---|
| `application.yml` | Configuration partagee par tous les clients (URL Eureka, exposition Actuator) |
| `product-service.yml` | Configuration specifique a `product-service` (port, datasource H2, JPA) |
| `order-service.yml` | Configuration specifique a `order-service` (port, datasource H2, JPA) |
| `api-gateway.yml` | Configuration specifique a `api-gateway` (port, routes) |

## Verifier

Recuperer la configuration effective d'un service donne :

```bash
curl http://localhost:8888/product-service/default
curl http://localhost:8888/order-service/default
curl http://localhost:8888/api-gateway/default
```

## Notes

- A demarrer **avant** `api-gateway`, `product-service` et `order-service` : ces derniers appellent le config-server au demarrage (`spring.config.import=optional:configserver:...`, prefixe `optional:` pour ne pas bloquer si le config-server est indisponible).
- Modifier un fichier dans `config-repo/` puis redemarrer le service concerne (ou appeler `POST /actuator/refresh` sur le service si le support du refresh dynamique est active) suffit a propager le changement.

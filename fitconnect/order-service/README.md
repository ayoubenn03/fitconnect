# order-service

Microservice de gestion des commandes, persiste dans une base **H2** en memoire. Pour chaque article d'une commande, il interroge `product-service` (via Feign + Eureka, load-balance automatiquement) afin de valider l'existence du produit et de recuperer son nom/prix au moment de la commande.

## Port

`8082`

## Base de donnees

- H2 en memoire (`jdbc:h2:mem:orderdb`).
- Console H2 : `http://localhost:8082/h2-console` (JDBC URL : `jdbc:h2:mem:orderdb`, user `sa`, pas de mot de passe).

## Documentation API (Swagger / OpenAPI)

- Swagger UI : `http://localhost:8082/swagger-ui.html`
- Spec JSON : `http://localhost:8082/v3/api-docs`

## Endpoints

| Methode | URL | Description |
|---|---|---|
| GET | `/api/orders` | Lister toutes les commandes |
| GET | `/api/orders/{id}` | Recuperer une commande |
| POST | `/api/orders` | Creer une commande (chaque article est verifie via product-service) |
| PATCH | `/api/orders/{id}/status` | Changer le statut (`CREATED`, `CONFIRMED`, `CANCELLED`) |
| DELETE | `/api/orders/{id}` | Supprimer une commande |

### Exemple de requete

```bash
curl -X POST http://localhost:8082/api/orders \
  -H "Content-Type: application/json" \
  -d '{"customerName":"Alice","items":[{"productId":1,"quantity":2}]}'
```

Si `productId` n'existe pas dans `product-service`, la reponse est `400 Bad Request` avec un message explicite. Si `product-service` est injoignable, la reponse est `502 Bad Gateway`.

## Demarrage

**Prerequis** : `product-service` et idealement `eureka-server` doivent etre demarres, sinon la creation de commande echouera (le client Feign resout `product-service` via le nom logique enregistre dans Eureka).

```bash
mvn spring-boot:run
```

## Tests

```bash
mvn test
```

- `OrderServiceTest` : tests unitaires de la couche service, avec `OrderRepository` et `ProductClient` (Feign) mockes.
- `OrderControllerIntegrationTest` : tests d'integration (`MockMvc`) sur une base H2 dediee aux tests, `ProductClient` mocke via `@MockBean` pour ne pas dependre d'un `product-service` reellement lance. Couvre le cycle de vie complet, le cas produit inexistant (400) et la validation des entrees.

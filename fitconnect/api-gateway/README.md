# api-gateway

Point d'entree HTTP unique de l'architecture (Spring Cloud Gateway, reactif). Route les requetes vers les microservices en utilisant leur nom logique enregistre dans `eureka-server` (`lb://product-service`, `lb://order-service`), sans jamais coder une IP/port en dur.

## Port

`8080`

## Routes

| Prefixe entrant | Redirige vers |
|---|---|
| `/api/products/**` | `product-service` (load-balance via Eureka) |
| `/api/orders/**` | `order-service` (load-balance via Eureka) |

La decouverte automatique de routes est egalement activee (`spring.cloud.gateway.discovery.locator.enabled=true`) : tout service enregistre dans Eureka est aussi joignable via `/<nom-service-en-minuscule>/**`.

## Demarrage

```bash
mvn spring-boot:run
```

Ordre de demarrage recommande : `config-server` -> `eureka-server` -> `api-gateway` (+ `product-service`, `order-service`).

## Exemple d'appel via la gateway

```bash
curl http://localhost:8080/api/products
curl http://localhost:8080/api/orders
```

## Tests

Un test de contexte (`ApiGatewayApplicationTests`) verifie que l'application demarre correctement, config-server et eureka-server desactives pour rester independant de l'infrastructure lors du build.

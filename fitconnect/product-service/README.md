# product-service

Microservice CRUD de gestion du catalogue produits, persiste dans une base **H2** en memoire.

## Port

`8081`

## Base de donnees

- H2 en memoire (`jdbc:h2:mem:productdb`), recreee a chaque demarrage.
- Console H2 : `http://localhost:8081/h2-console` (JDBC URL : `jdbc:h2:mem:productdb`, user `sa`, pas de mot de passe).
- 4 produits de demonstration charges automatiquement au demarrage (`src/main/resources/data.sql`).

## Documentation API (Swagger / OpenAPI)

- Swagger UI : `http://localhost:8081/swagger-ui.html`
- Spec JSON : `http://localhost:8081/v3/api-docs`

## Endpoints

| Methode | URL | Description |
|---|---|---|
| GET | `/api/products` | Lister tous les produits |
| GET | `/api/products/{id}` | Recuperer un produit |
| POST | `/api/products` | Creer un produit |
| PUT | `/api/products/{id}` | Mettre a jour un produit |
| DELETE | `/api/products/{id}` | Supprimer un produit |

### Exemple de requete

```bash
curl -X POST http://localhost:8081/api/products \
  -H "Content-Type: application/json" \
  -d '{"name":"Tapis de souris","description":"Tapis XXL","price":14.90,"quantity":40}'
```

## Demarrage

```bash
mvn spring-boot:run
```

Fonctionne seul (config par defaut dans `src/main/resources/application.yml`), ou avec `config-server` + `eureka-server` demarres pour beneficier de la configuration centralisee et de l'enregistrement dans l'annuaire.

## Tests

```bash
mvn test
```

- `ProductServiceTest` : tests unitaires de la couche service avec `ProductRepository` mocke (Mockito).
- `ProductControllerIntegrationTest` : tests d'integration bout en bout (`MockMvc`) sur une base H2 dediee aux tests, couvrant le cycle de vie CRUD complet, la validation des entrees et les cas d'erreur 404.

# eureka-server

Serveur d'annuaire (**service discovery**) base sur Netflix Eureka. Chaque microservice s'enregistre ici au demarrage avec son nom logique et son URL ; les autres services (et la gateway) interrogent cet annuaire au lieu de coder des URLs en dur.

## Port

`8761`

## Demarrage

```bash
mvn spring-boot:run
```

## Verifier

Console web Eureka (liste des services enregistres et leurs instances) :

```
http://localhost:8761
```

## Notes

- `register-with-eureka: false` et `fetch-registry: false` : le serveur lui-meme n'est pas un client, il ne s'enregistre pas et ne recupere pas de registre.
- A demarrer **avant** les autres services (sauf `config-server`, independant de lui) pour qu'ils puissent s'enregistrer des leur lancement.

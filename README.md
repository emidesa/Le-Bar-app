# Bar'app

Application web de commande de cocktails pour bars. Les clients commandent depuis leur table via QR code, les barmakers préparent et font avancer les commandes étape par étape.

## Prérequis

- [Docker](https://www.docker.com/) et Docker Compose

## Lancer l'application

```bash
docker compose up --build
```

Le premier démarrage prend quelques minutes (build des images + chargement de la carte des cocktails depuis TheCocktailDB).

| Service  | URL                          |
|----------|------------------------------|
| Frontend | http://localhost             |
| Backend  | http://localhost:8081/api    |

## Utilisation

### Côté Barmaker
1. Accéder à http://localhost
2. Créer un compte barmaker via **S'inscrire**
3. Gérer la carte (catégories et cocktails) depuis le tableau de bord
4. Traiter les commandes en faisant avancer chaque cocktail étape par étape

### Côté Client
1. Scanner le QR code de la table (ou accéder à http://localhost)
2. Parcourir la carte, filtrer par catégorie
3. Ajouter des cocktails au panier et passer commande
4. Suivre l'avancement en temps réel

## Stack technique

| Couche    | Technologie                          |
|-----------|--------------------------------------|
| Backend   | Java 25 · Spring Boot 4 · Spring Security · JWT |
| Base de données | PostgreSQL 15                  |
| Frontend  | Vue.js 3 · TypeScript · Pinia · Vite |
| Conteneurs | Docker · Docker Compose · Nginx     |

## Tests

**Backend** — JUnit 5 + Mockito (69 tests, couverture JaCoCo : 89.5%)
```bash
cd backend && mvn test
```

**Frontend** — Vitest (21 tests)
```bash
cd frontend && npm run test:unit
```

## Développement local (sans Docker)

**Prérequis** : Java 25, Maven, Node.js 22+, PostgreSQL

```bash
# Base de données
psql -U postgres -c "CREATE DATABASE barapp; CREATE USER barapp WITH PASSWORD 'barapp'; GRANT ALL ON DATABASE barapp TO barapp;"

# Backend (port 8081)
cd backend && mvn spring-boot:run

# Frontend (port 5173)
cd frontend && npm install && npm run dev
```

## Structure du projet

```
Examen CDA/
├── backend/              # API Spring Boot
│   ├── src/main/java/    # Code source
│   ├── src/test/java/    # Tests JUnit
│   └── Dockerfile
├── frontend/             # Application Vue.js
│   ├── src/
│   │   ├── stores/       # Pinia (auth, cart, menu, orders)
│   │   ├── views/        # Vues client et barmaker
│   │   └── __tests__/    # Tests Vitest
│   └── Dockerfile
├── docker-compose.yml
└── README.md
```

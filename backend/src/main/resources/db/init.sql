-- ============================================================
-- Bar'app — Script d'initialisation de la base de données
-- ============================================================
-- Ce script s'exécute automatiquement au premier démarrage de
-- PostgreSQL (répertoire docker-entrypoint-initdb.d/).
-- Hibernate (ddl-auto=update) prend le relais au démarrage de
-- l'application pour mettre à jour le schéma si besoin.
-- ============================================================

-- Utilisateurs (barmakers)
CREATE TABLE IF NOT EXISTS users (
    id          BIGSERIAL PRIMARY KEY,
    email       VARCHAR(100)  NOT NULL UNIQUE,
    password    VARCHAR(255)  NOT NULL,
    role        VARCHAR(20)   NOT NULL,
    created_at  TIMESTAMP,
    updated_at  TIMESTAMP
);

-- Catégories de cocktails
CREATE TABLE IF NOT EXISTS categories (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(100)  NOT NULL UNIQUE,
    description TEXT,
    created_at  TIMESTAMP
);

-- Cocktails
CREATE TABLE IF NOT EXISTS cocktails (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(150)  NOT NULL,
    description TEXT,
    image_url   VARCHAR(255),
    category_id BIGINT REFERENCES categories(id),
    created_at  TIMESTAMP,
    updated_at  TIMESTAMP
);

-- Ingrédients
CREATE TABLE IF NOT EXISTS ingredients (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(100)  NOT NULL UNIQUE,
    created_at  TIMESTAMP
);

-- Association cocktail ↔ ingrédients (N:N)
CREATE TABLE IF NOT EXISTS cocktail_ingredients (
    id            BIGSERIAL PRIMARY KEY,
    cocktail_id   BIGINT         NOT NULL REFERENCES cocktails(id) ON DELETE CASCADE,
    ingredient_id BIGINT         NOT NULL REFERENCES ingredients(id),
    quantity      DECIMAL(8, 2),
    unit          VARCHAR(20)    NOT NULL,
    notes         TEXT,
    created_at    TIMESTAMP
);

-- Tailles et prix d'un cocktail (S / M / L)
CREATE TABLE IF NOT EXISTS cocktail_sizes (
    id          BIGSERIAL PRIMARY KEY,
    cocktail_id BIGINT        NOT NULL REFERENCES cocktails(id) ON DELETE CASCADE,
    size        VARCHAR(10)   NOT NULL,
    price       DECIMAL(6, 2) NOT NULL,
    created_at  TIMESTAMP
);

-- Commandes (identifiées par numéro de table via QR code)
CREATE TABLE IF NOT EXISTS orders (
    id           BIGSERIAL PRIMARY KEY,
    table_number INTEGER        NOT NULL,
    status       VARCHAR(20)    NOT NULL DEFAULT 'COMMANDEE',
    total_price  DECIMAL(10, 2) NOT NULL DEFAULT 0,
    created_at   TIMESTAMP,
    updated_at   TIMESTAMP,
    completed_at TIMESTAMP
);

-- Items d'une commande
CREATE TABLE IF NOT EXISTS order_items (
    id          BIGSERIAL PRIMARY KEY,
    order_id    BIGINT        NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    cocktail_id BIGINT        NOT NULL REFERENCES cocktails(id),
    size_id     BIGINT        NOT NULL REFERENCES cocktail_sizes(id),
    quantity    INTEGER       NOT NULL DEFAULT 1,
    unit_price  DECIMAL(6, 2) NOT NULL,
    status      VARCHAR(30)   NOT NULL DEFAULT 'ATTENTE',
    created_at  TIMESTAMP
);

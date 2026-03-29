-- ============================================================
-- Product Service - Script de inicialización de base de datos
-- ============================================================

CREATE DATABASE product_service_db;

\c product_service_db;

-- Tipo ENUM para categorías de ropa
CREATE TYPE product_category AS ENUM (
    'CAMISETAS',
    'PANTALONES',
    'VESTIDOS',
    'FALDAS',
    'ABRIGOS',
    'CALZADO',
    'ACCESORIOS',
    'ROPA_INTERIOR',
    'DEPORTIVA',
    'OTROS'
);

-- Tabla principal de productos
CREATE TABLE products (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(255)        NOT NULL,
    description TEXT,
    price       NUMERIC(10, 2)      NOT NULL CHECK (price > 0),
    stock       INTEGER             NOT NULL CHECK (stock >= 0),
    category    product_category    NOT NULL,
    size        VARCHAR(20),
    color       VARCHAR(50),
    brand       VARCHAR(100),
    image_url   VARCHAR(500),
    active      BOOLEAN             NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP           NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP           NOT NULL DEFAULT NOW()
);

-- Índices para las consultas más frecuentes
CREATE INDEX idx_products_active         ON products (active);
CREATE INDEX idx_products_category       ON products (category) WHERE active = TRUE;
CREATE INDEX idx_products_name_search    ON products USING gin (to_tsvector('spanish', name));

-- Función para actualizar updated_at automáticamente
CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_products_updated_at
    BEFORE UPDATE ON products
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

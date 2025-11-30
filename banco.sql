CREATE DATABASE borghi_doro;

CREATE SCHEMA IF NOT EXISTS borghi;
SET search_path TO borghi, public;


CREATE TABLE usuarios (
    id SERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    senha_hash TEXT NOT NULL,
    telefone VARCHAR(20),
    data_nascimento DATE,
    data_cadastro TIMESTAMP DEFAULT NOW(),
    deletado BOOLEAN DEFAULT false
);

CREATE TABLE casas (
    id SERIAL PRIMARY KEY,
    titulo VARCHAR(150) NOT NULL,
    localidade VARCHAR(100) NOT NULL,
    regiao VARCHAR(50) NOT NULL,
    preco DECIMAL(10,2) DEFAULT 1.00,
    imagem_url TEXT NOT NULL,
    disponivel BOOLEAN DEFAULT true,
    deletado BOOLEAN DEFAULT false
);

CREATE TABLE reservas (
    id SERIAL PRIMARY KEY,
    usuario_id INTEGER NOT NULL REFERENCES usuarios(id),
    casa_id INTEGER NOT NULL REFERENCES casas(id),
    data_reserva TIMESTAMP DEFAULT NOW(),
    valor_total DECIMAL(10,2) DEFAULT 1.00,
    status VARCHAR(20) DEFAULT 'Pendente' CHECK (status IN ('Pendente','Confirmada','Cancelada')),
    metodo_pagamento VARCHAR(20),
    deletado BOOLEAN DEFAULT false
);



CREATE OR REPLACE FUNCTION soft_delete_usuario() RETURNS TRIGGER AS $$
BEGIN
    IF NEW.deletado = true AND OLD.deletado = false THEN
        UPDATE reservas SET deletado = true WHERE usuario_id = OLD.id;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_soft_delete_usuario
    BEFORE UPDATE OF deletado ON usuarios
    FOR EACH ROW WHEN (NEW.deletado = true AND OLD.deletado = false)
    EXECUTE FUNCTION soft_delete_usuario();


CREATE OR REPLACE FUNCTION evita_reserva_duplicada() RETURNS TRIGGER AS $$
BEGIN
    IF EXISTS (SELECT 1 FROM reservas WHERE usuario_id = NEW.usuario_id AND casa_id = NEW.casa_id AND deletado = false) THEN
        RAISE EXCEPTION 'Você já reservou esta casa!';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_evita_duplicada
    BEFORE INSERT ON reservas FOR EACH ROW
    EXECUTE FUNCTION evita_reserva_duplicada();


CREATE OR REPLACE FUNCTION bloqueia_delete() RETURNS TRIGGER AS $$
BEGIN
    RAISE EXCEPTION 'Exclusão física proibida! Use deletado = true';
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER no_delete_usuarios BEFORE DELETE ON usuarios FOR EACH ROW EXECUTE FUNCTION bloqueia_delete();
CREATE TRIGGER no_delete_casas BEFORE DELETE ON casas FOR EACH ROW EXECUTE FUNCTION bloqueia_delete();
CREATE TRIGGER no_delete_reservas BEFORE DELETE ON reservas FOR EACH ROW EXECUTE FUNCTION bloqueia_delete();


CREATE OR REPLACE PROCEDURE sp_cadastrar_usuario(p_nome VARCHAR, p_email VARCHAR, p_senha TEXT, p_telefone VARCHAR, p_nascimento DATE)
LANGUAGE plpgsql AS $$
BEGIN
    INSERT INTO usuarios(nome,email,senha_hash,telefone,data_nascimento)
    VALUES(p_nome,p_email,p_senha,p_telefone,p_nascimento);
END;
$$;

CREATE OR REPLACE PROCEDURE sp_reservar_casa(p_usuario_id INT, p_casa_id INT, p_metodo VARCHAR)
LANGUAGE plpgsql AS $$
DECLARE v_preco DECIMAL(10,2);
BEGIN
    SELECT preco INTO v_preco FROM casas WHERE id = p_casa_id AND disponivel = true AND deletado = false;
    IF v_preco IS NULL THEN RAISE EXCEPTION 'Casa indisponível'; END IF;
    INSERT INTO reservas(usuario_id,casa_id,valor_total,metodo_pagamento)
    VALUES(p_usuario_id,p_casa_id,v_preco,p_metodo);
END;
$$;


INSERT INTO casas(titulo,localidade,regiao,imagem_url) VALUES
('Casa de Pedra com Terraço','Sambuca di Sicilia','Sicília','https://images.unsplash.com/photo-1578662996442-48f60103fcbf'),
('Casa com Vista para os Vinhedos','Monticiano','Toscana','https://images.unsplash.com/photo-1564013799919-ab600027ffc6'),
('Casa à Beira-Mar','Tropea','Calábria','https://images.unsplash.com/photo-1582719478250-c89cae4dc85b'),
('Casa com Vista para as Montanhas','Gangi','Sicília','https://images.unsplash.com/photo-1505843513577-22bb7d21e455');
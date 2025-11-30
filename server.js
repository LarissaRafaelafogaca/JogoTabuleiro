require("dotenv").config();
const express = require("express");
const { Pool } = require("pg");
const bcrypt = require("bcrypt");
const cors = require("cors");

const app = express();
app.use(cors());
app.use(express.json());

const pool = new Pool({
  host: process.env.DB_HOST,
  user: process.env.DB_USER,
  password: process.env.DB_PASS,
  database: process.env.DB_NAME,
  port: process.env.DB_PORT,
});

// CADASTRO
app.post("/api/register", async (req, res) => {
  const { nome, email, senha, telefone, data_nascimento } = req.body;
  try {
    const hash = await bcrypt.hash(senha, 10);
    await pool.query(`CALL borghi.sp_cadastrar_usuario($1,$2,$3,$4,$5)`, [
      nome,
      email,
      hash,
      telefone,
      data_nascimento,
    ]);
    res.json({ success: true, message: "Cadastro realizado com sucesso!" });
  } catch (err) {
    res.json({ success: false, message: err.message || "E-mail já existe" });
  }
});

// LOGIN
app.post("/api/login", async (req, res) => {
  const { email, senha } = req.body;
  try {
    const { rows } = await pool.query(
      `SELECT id, nome, senha_hash FROM borghi.usuarios WHERE email = $1 AND deletado = false`,
      [email]
    );
    if (rows.length === 0)
      return res.json({ success: false, message: "Usuário não encontrado" });
    const ok = await bcrypt.compare(senha, rows[0].senha_hash);
    if (ok) res.json({ success: true, userId: rows[0].id, nome: rows[0].nome });
    else res.json({ success: false, message: "Senha incorreta" });
  } catch (err) {
    res.status(500).json({ success: false });
  }
});

// LISTAR CASAS
app.get("/api/casas", async (req, res) => {
  const { rows } = await pool.query(
    `SELECT id, titulo, localidade, regiao, preco, imagem_url FROM borghi.casas WHERE disponivel AND NOT deletado`
  );
  res.json(rows);
});

// RESERVAR
app.post("/api/reservar", async (req, res) => {
  const { usuario_id, casa_id } = req.body;
  try {
    await pool.query(`CALL borghi.sp_reservar_casa($1,$2,$3)`, [
      usuario_id,
      casa_id,
      "pendente",
    ]);
    res.json({ success: true });
  } catch (err) {
    res.json({ success: false, message: err.message });
  }
});

// MINHAS RESERVAS (CARRINHO)
app.get("/api/minhas-reservas/:id", async (req, res) => {
  const { rows } = await pool.query(
    `SELECT r.valor_total, c.titulo, c.localidade, c.imagem_url
     FROM borghi.reservas r
     JOIN borghi.casas c ON r.casa_id = c.id
     WHERE r.usuario_id = $1 AND r.status = 'Pendente' AND NOT r.deletado`,
    [req.params.id]
  );
  res.json(rows);
});

app.listen(process.env.PORT, () => {
  console.log(`API rodando na porta ${process.env.PORT}`);
});

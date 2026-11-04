# IANA — Organizador de Finanças Pessoais

O **IANA** é uma aplicação completa para gestão e controle financeiro pessoal, desenvolvida com arquitetura hexagonal no backend (Spring Boot 3 + Java 21) e interface moderna e intuitiva no frontend (Angular 17 Standalone).

---

## 🎨 Tema e Visual

O sistema adota uma identidade visual clara em **Azul Real & Branco** (`#2563eb`, `#1e3a8a`, `#f0f6ff` e `#ffffff`), com foco em legibilidade, contraste e clareza nos resumos de saldo, receitas e despesas.

---

## ✨ Funcionalidades Principais

- **Isolamento de Dados por Usuário (Multi-tenant)**:
  - Cada usuário registrado e autenticado acessa, visualiza e manipula exclusivamente seus próprios lançamentos, categorias e saldos.
- **Gestão de Despesas & Saldos**:
  - Cadastro de despesas com nome, valor, descrição e categoria opcional.
  - Cadastro de fontes de saldo/entradas com cálculo em tempo real do **Saldo Líquido**.
- **Importação Inteligente de Faturas e Boletos (Sem Template)**:
  - Suporte a extratos em **CSV** e **PDF** de qualquer instituição bancária (Nubank, Banco Inter, Itaú, C6, Bradesco, Santander, etc.).
  - Detecção inteligente de colunas (datas, estabelecimentos, valores positivos/negativos, categorias).
- **Categorização Personalizada**:
  - Criação e manutenção de categorias exclusivas de cada usuário.
- **Autenticação Segura & Recuperação de Senha**:
  - Cadastro com validação de senha forte (mínimo 8 caracteres, maiúscula, número e caractere especial).
  - Confirmação de conta por código de verificação numérico de 6 dígitos enviado por e-mail.
  - Fluxo de **Esqueci Minha Senha** com validação de token por e-mail e redefinição direta na tela.

---

## 🚀 Tecnologias Utilizadas

### Backend
- **Java 21** & **Spring Boot 3.4.1**
- **Arquitetura Hexagonal (Ports & Adapters)**
- **Spring Security** com JWT (Nimbus JOSE) & BCrypt
- **Spring Data JPA** & Banco de Dados H2 (em memória / arquivo)
- **Apache PDFBox** para extração de extratos bancários e boletos em PDF

### Frontend
- **Angular 17** (Standalone Components)
- **TypeScript**, **RxJS** e **HTML5/CSS3**
- Layout responsivo com design limpo em tons de azul e branco

---

## 🛠️ Como Executar o Projeto Localmente

### Pré-requisitos
- **Java 21** instalado
- **Node.js** (v18+) e **npm** instalados

### 1. Backend (Spring Boot)
```bash
# Na raiz do projeto:
./mvnw clean spring-boot:run
```
O servidor estará disponível em: `http://localhost:8080`  
Console H2 disponível em: `http://localhost:8080/h2-console`

### 2. Frontend (Angular)
```bash
cd frontend
npm install
npm start
```
Acesse a aplicação em: `http://localhost:4200`

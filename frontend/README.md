# Frontend — Financial Management API

Interface web estática (HTML + Bootstrap + jQuery, template [SB Admin 2](https://startbootstrap.com/theme/sb-admin-2)) que consome os dois microsserviços (MS1 e MS2) diretamente do navegador. **Não é parte do escopo exigido pelo PO** — foi adicionada como bônus visual além dos nove pedidos obrigatórios, que são demonstrados via Swagger/Postman.

## Como rodar

1. Suba o backend normalmente:
   ```bash
   docker compose up --build
   ```
2. Abra `frontend/login.html` em um navegador. Pode ser:
   - Duplo clique no arquivo (abre via `file://`), ou
   - Uma extensão "Live Server" do VS Code (recomendado, evita eventuais bloqueios de `file://` em alguns navegadores)
3. Login com um usuário de demonstração (ex.: `ana.silva` / `user1123`).

## Configuração

As URLs dos microsserviços estão em `js/config.js`:

```js
const API_CONFIG = {
    MS1_URL: "http://localhost:8080",
    MS2_URL: "http://localhost:8081"
};
```

Ajuste se as portas do `.env` (`MS1_APP_PORT` / `MS2_APP_PORT`) forem diferentes das padrão.

## Páginas

| Arquivo | Descrição |
|---|---|
| `login.html` | Autenticação (username ou email) contra o MS1 |
| `index.html` | Dashboard: saldo/limites (MockAPI), gasto por dia e por categoria (gráficos Chart.js), últimas transações |
| `transactions.html` | Listagem com filtros, criação de nova transação (modal) e download do relatório CSV |

## Observações técnicas

- Todas as bibliotecas de terceiros (jQuery, Bootstrap, Font Awesome, Chart.js, DataTables) são carregadas via **CDN** (cdnjs.cloudflare.com), não versionadas no repositório. É por isso que clonar o projeto e abrir os HTMLs já funciona sem nenhum passo extra de instalação — só precisa de internet para baixar essas libs na primeira carga da página.
- O token JWT é guardado no `localStorage` do navegador (`js/app.js`, objeto `Auth`).
- Foi necessário habilitar CORS no `SecurityConfig` de ambos os microsserviços (`CorsConfigurationSource`) para que o navegador aceite as chamadas `fetch` partindo de uma origem diferente (`file://` ou outra porta).
- Esse frontend é completamente dissociado do backend: se ele falhar ou não for usado, o backend continua 100% funcional e demonstrável via Swagger/Postman, como o PO exige.

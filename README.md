# Back Albion — Guild Treasury API

API Spring Boot para la tesorería del gremio **II TEMPUS FUGIT II** (Avalonianas, balance, multas, solicitudes).

## Requisitos

- Java 21
- Maven 3.9+
- PostgreSQL (Supabase recomendado)

## Variables de entorno

Copia `.env.example` a `.env`:

```bash
DB_HOST=
DB_PORT=5432
DB_NAME=
DB_USER=
DB_PASSWORD=
DB_SSLMODE=require
JWT_SECRET=
JWT_EXPIRATION_MS=86400000
SERVER_PORT=8080
GUILD_NAME=II TEMPUS FUGIT II
```

## Desarrollo local

```bash
mvn spring-boot:run
```

API: `http://localhost:8080`  
Swagger: `http://localhost:8080/swagger-ui.html`

## Docker

```bash
docker build -t back-albion .
docker run -p 8080:8080 --env-file .env back-albion
```

## Despliegue (Railway / Render / Fly.io)

1. Conecta este repositorio.
2. Build: Dockerfile incluido o `mvn clean package -DskipTests`.
3. Start: `java -jar target/guild-balance-system-1.0.0-SNAPSHOT.jar`
4. Configura todas las variables de `.env.example` en el panel del hosting.
5. Puerto: `8080` (o `SERVER_PORT`).

### Supabase + Render

Si ves `max clients reached in session mode - pool_size: 15`:

- En Supabase → **Settings → Database → Connection string**, elige **Transaction pooler** y puerto **6543** (no Session/5432).
- Pon `DB_POOL_MAX=5` en Render.
- Cierra el backend local si también apunta a la misma BD.
- Espera 1–2 min y redeploy (las conexiones zombie de intentos fallidos se liberan solas).

### Mantener el servicio free activo en Render

Render apaga instancias free tras ~15 min sin peticiones. Este repo incluye `.github/workflows/keep-render-alive.yml` que hace ping cada 10 min.

1. En GitHub → repo **albion-back** → **Settings → Secrets and variables → Actions**
2. Crea el secret `RENDER_BACKEND_URL` con la URL de Render, sin barra final:  
   `https://tu-servicio.onrender.com`
3. El workflow corre solo; puedes probarlo en **Actions → Keep Render alive → Run workflow**

También puedes usar [UptimeRobot](https://uptimerobot.com) (gratis, cada 5 min) apuntando a `https://tu-servicio.onrender.com/api/health`.

## CORS

El front en Vercel llama al back en otro dominio. Configura en Render:

```
CORS_ALLOWED_ORIGINS=https://albion-front.vercel.app,http://localhost:4200
```

**Importante:** CORS usa el **origen** (dominio), no la ruta.  
`https://albion-front.vercel.app/auth/login` → origen permitido: `https://albion-front.vercel.app`

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

## CORS

Si el front está en otro dominio, configura CORS en producción apuntando a la URL del frontend.

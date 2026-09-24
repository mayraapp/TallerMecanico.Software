# AutoManager — Taller mecánico

Proyecto web del taller mecánico. La entrega actual corresponde a **M01: seguridad, autenticación, autorización, roles y usuarios**.

La documentación completa de la fase, los módulos terminados, las tablas, los archivos generados, la ubicación segura de la configuración local y las instrucciones de publicación está en [Planeacion_FaseInicial.md](Planeacion_FaseInicial.md).

## Estructura

```text
frontend/  Aplicación Vue 3 + Vite
backend/   API Spring Boot + Java 26
database/  Referencia de esquema; Flyway conserva las migraciones reales en backend/
```

## Ejecución local

```bash
# Backend
cd backend
./mvnw spring-boot:run

# Frontend, en otra terminal
cd frontend
npm run dev
```

Antes de ejecutar el backend se debe crear `backend/.env` a partir de `.env.example`. Ese archivo contiene secretos locales y no se versiona.

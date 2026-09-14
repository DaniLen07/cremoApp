# Cremo / Deli

Aplicacion web para registrar ventas e inventario de arroz con leche. La interfaz se sirve desde Spring Boot y los datos se guardan en MySQL.

## Requisitos

- Java 17 o superior
- Maven 3.9 o superior
- MySQL 8

## Ejecutar en Windows

1. Crea la base de datos y ejecuta `database/schema.sql` en MySQL. Si ya existe, ejecuta las migraciones necesarias: `database/migration-payment-method.sql` y `database/migration-toppings.sql`.
2. Abre una terminal en esta carpeta (`deli`).
3. Define las variables de conexion. Ajusta la contrasena a la de tu instalacion:

```powershell
$env:DB_HOST="localhost"
$env:DB_PORT="3306"
$env:DB_NAME="bdDeli"
$env:DB_USERNAME="root"
$env:DB_PASSWORD="TU_CONTRASENA_MYSQL"
$env:DB_SSL="false"
$env:APP_ADMIN_USERNAME="TU_USUARIO_ADMIN"
$env:APP_ADMIN_PASSWORD="TU_CLAVE_ADMIN_SEGURA"
$env:APP_SELLER_USERNAME="TU_USUARIO_VENDEDOR"
$env:APP_SELLER_PASSWORD="TU_CLAVE_VENDEDOR_SEGURA"
$env:APP_COOKIE_SECURE="false"
```

4. Inicia la aplicacion:

```powershell
mvn spring-boot:run
```

Abre http://localhost:8080/ en el PC.

Al iniciar sesión, el administrador puede gestionar inventario, precio, reportes y ventas. El vendedor solo puede registrar ventas. Cada vendedor tiene un usuario y contraseña diferente, registrados por el administrador desde el panel de equipo.

El administrador puede registrar vendedores desde el panel de equipo con nombre, teléfono, usuario y contraseña. Los vendedores registrados se guardan en MySQL, aparecen en el selector de ventas y pueden iniciar sesión inmediatamente sin reiniciar la aplicación. Las contraseñas se almacenan cifradas y nunca se muestran.

El acceso comienza en `/login.html`. Después del login, el administrador entra al panel principal (`/`) y cada vendedor entra a `/seller.html`. El vendedor ve sus unidades y total vendido del día, además de su historial; el administrador ve una tabla diaria de todos los vendedores.

El borrado automático de ventas está desactivado por defecto para conservar el historial durante reinicios y despliegues. Si se necesita activarlo de forma excepcional, define `DATA_RESET_ENABLED=true` y ajusta `DATA_RESET_INTERVAL_MS`; el inventario, los productos, precios y vendedores se conservan.

## Abrir desde un celular en la misma red

1. Obtiene la IP local del PC con `ipconfig` (por ejemplo, `192.168.1.25`).
2. Inicia con acceso de red:

```powershell
$env:SERVER_ADDRESS="0.0.0.0"
mvn spring-boot:run
```

3. Permite el puerto 8080 en el Firewall de Windows si lo solicita.
4. Desde el celular conectado a la misma Wi-Fi abre `http://192.168.1.25:8080/`.

Para usarla desde fuera de la red local se necesita desplegarla en un hosting; no se debe publicar directamente el puerto de MySQL.

## Subir el proyecto a GitHub

GitHub guarda el codigo, pero no ejecuta esta aplicacion ni reemplaza la base de datos.

1. Crea una cuenta en GitHub y un repositorio nuevo, por ejemplo `cremo-deli`.
2. No marques la opcion de crear README, `.gitignore` ni licencia, porque estos archivos ya estan en el proyecto.
3. Abre PowerShell en la carpeta `deli` y ejecuta:

```powershell
git init
git branch -M main
git add .
git commit -m "Version inicial de Cremo"
git remote add origin https://github.com/TU_USUARIO/cremo-deli.git
git push -u origin main
```

El archivo `.gitignore` evita subir `target`, logs y archivos `.env`. Nunca pongas `DB_PASSWORD` en el repositorio. Si alguna contrasena estuvo en una version anterior o fue compartida, cambiala antes del despliegue.

En producción usa `APP_COOKIE_SECURE=true` porque el acceso será mediante HTTPS. Configura siempre `DB_HOST`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`, `APP_ADMIN_USERNAME` y `APP_ADMIN_PASSWORD`. No configures `APP_SELLER_USERNAME` ni `APP_SELLER_PASSWORD`: los vendedores se almacenan individualmente en MySQL con contraseñas cifradas.

Para publicar cambios posteriormente:

```powershell
git add .
git commit -m "Actualiza la aplicacion"
git push
```

## Pruebas y empaquetado

```powershell
mvn test
mvn clean package -DskipTests
java -jar target/cremo-0.0.1-SNAPSHOT.jar
```

## Despliegue sin tarjeta: Render + PostgreSQL

Esta configuración usa un servicio web gratuito de Render y una base PostgreSQL gratuita externa, por ejemplo Neon o Supabase. Render Free puede suspender la aplicación tras un periodo sin visitas; la primera solicitud posterior puede tardar aproximadamente un minuto mientras Spring Boot vuelve a iniciar. Los datos permanecen en PostgreSQL.

1. Crea una base PostgreSQL en Neon o Supabase y guarda su host, puerto, nombre, usuario y contraseña. Configúrala para aceptar conexiones externas y usa SSL.
2. Ejecuta `database/schema-postgresql.sql` una sola vez en esa base. Si vas a conservar ventas de MySQL, exporta los datos y conviértelos antes de importarlos; no mezcles directamente ambos esquemas.
3. Sube el proyecto a GitHub sin incluir contraseñas ni archivos `.env`.
4. En Render crea un **Web Service** desde el repositorio.
5. Configura:

- Build command: `mvn clean package -DskipTests`
- Start command: `java -jar target/cremo-0.0.1-SNAPSHOT.jar`
- Runtime: Java 17

6. Agrega estas variables privadas en Render:

```text
DB_HOST=host-de-la-base
DB_PORT=5432
DB_NAME=nombre_de_la_base
DB_USERNAME=usuario
DB_PASSWORD=contrasena
DB_SSL_MODE=require
DB_DRIVER=org.postgresql.Driver
APP_ADMIN_USERNAME=usuario-admin
APP_ADMIN_PASSWORD=contrasena-admin-segura
APP_COOKIE_SECURE=true
```

Si el proveedor entrega una URL JDBC completa, también puedes configurar `SPRING_DATASOURCE_URL` con formato `jdbc:postgresql://host:5432/base?sslmode=require`.

7. Despliega y genera el dominio público de Render. El puerto ya se obtiene automáticamente desde `PORT`.
8. Prueba el login, inventario, registro de una venta, recarga de página y persistencia de los datos.

No guardes datos en archivos dentro de Render y no actives `DATA_RESET_ENABLED`. La sesión puede perderse cuando el servicio se suspende y el usuario tendrá que iniciar sesión de nuevo.

## Comprobacion despues del despliegue

1. Abre la URL publica y confirma que aparezca el panel.
2. Define inventario y precio.
3. Registra una venta con un vendedor y `Efectivo` o `Nequi`.
4. Recarga la pagina y confirma que la venta siga visible.
5. Consulta la tabla `sales` en PostgreSQL para comprobar la persistencia.

Si la pagina no abre, revisa los logs del servicio web. Si aparece un error de conexion, revisa `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`, `DB_SSL_MODE` y que PostgreSQL permita conexiones desde Render.

## Reglas de la aplicacion

- Medios de pago disponibles: `Efectivo` y `Nequi`.
- Vendedores disponibles: `Juan Diego`, `Christopher`, `Salome`, `Daniel`, `Luisa` y `Otro`.
- Las ventas, inventario y precio se guardan en la base configurada mediante JPA; Render usa PostgreSQL.
- Antes de desplegar, rota cualquier contrasena que haya estado escrita en archivos o compartida.

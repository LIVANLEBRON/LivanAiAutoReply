# Livan AI AutoReply

[![Download APK](https://img.shields.io/badge/Descargar-APK%20v1.0.0-success?style=for-the-badge&logo=android)](https://github.com/LIVANLEBRON/LivanAiAutoReply/releases/download/v1.0.0/LivanAiAutoReply.apk)

**Livan AI AutoReply** es una aplicación nativa para Android desarrollada en **Kotlin** y **Jetpack Compose (Material 3)**, diseñada para interceptar notificaciones de WhatsApp de contactos autorizados y responder automáticamente utilizando un modelo de inteligencia artificial local ejecutado en tu PC mediante **Ollama** a través de tu red privada **Tailscale**.

---

## 📥 Descarga Directa del APK

Puedes descargar e instalar directamente la última versión compilada:
* 📲 **[Descargar LivanAiAutoReply.apk (v1.0.0)](https://github.com/LIVANLEBRON/LivanAiAutoReply/releases/download/v1.0.0/LivanAiAutoReply.apk)**
* 📦 **[Página de Releases en GitHub](https://github.com/LIVANLEBRON/LivanAiAutoReply/releases/tag/v1.0.0)**

---

## 🚀 Características Principales

* **Filtro Estricto de Contactos Autorizados**: La aplicación únicamente responde a personas que hayas añadido explícitamente a la lista blanca. No responde a desconocidos ni a contactos no autorizados.
* **Integración Nativa con Ollama vía Tailscale**:
  * Envío de mensajes mediante `POST /api/chat`.
  * Parámetros configurables: IP/Host (ej. `100.x.x.x`), Puerto (ej. `3000` u `11434`), Endpoint, Nombre de Modelo, Timeout y Temperatura.
  * Botón para probar la conexión y listar los modelos disponibles en tu servidor mediante `GET /api/tags`.
* **Personalidades y Estilos de Respuesta**:
  * **Seca**: Respuestas breves, sin entusiasmo y minimalistas.
  * **Indiferente**: Poco interés conversacional, apático y desinteresado.
  * **Sarcástica**: Ironía moderada, comentarios agudos y humor sutil.
  * **Cortante**: Respuestas directas al grano, sin adornos ni rodeos.
  * **Natural**: Conversación fluida, amigable y humana (español dominicano natural relajado).
  * **Personalizada**: Instrucciones de System Prompt 100% redactadas por el usuario.
  * Personalidad asignable de forma global o individual por contacto.
* **Simulador de Conversación Integrado**: Prueba cómo respondería Ollama a un mensaje antes de activarlo en vivo.
* **Control y Seguridad**:
  * Interruptor maestro para encendido y apagado instantáneo del bot.
  * Interruptores individuales por contacto.
  * Protección anti-bucles: Detección de mensajes de eco propios para evitar loops infinitos.
  * Cooldown por contacto y límite máximo de respuestas por minuto.
  * Descarte de notificaciones de grupos (salvo autorización explícita).
  * Descarte de notificaciones antiguas (> 2 minutos).
  * Modo de simulación / confirmación manual opcional.
* **Sin Accesibilidad Invasiva**: Utiliza el estándar oficial de Android (`NotificationListenerService` y `RemoteInput`), sin simular pulsaciones ni interactuar con la pantalla.

---

## 📋 Requisitos

1. **Dispositivo Android**: Android 7.0 (API 24) o superior (optimizado para Android 14+ / Android 17 API 36/37).
2. **PC / Servidor**:
   * Linux (o Windows/macOS) con **Ollama** instalado y ejecutándose.
   * Al menos un modelo descargado (ej: `llama3.2`, `mistral`, `gemma2`).
3. **Tailscale**:
   * Instalado y activo tanto en tu PC/servidor como en tu teléfono Android, conectados a la misma red (Tailnet).

---

## 🛠️ Configuración de Ollama y Tailscale en tu PC

### 1. Permitir que Ollama escuche en tu IP de Tailscale o en toda la red
Por defecto, Ollama solo escucha en `127.0.0.1`. Para permitir que tu teléfono acceda mediante Tailscale:

En Linux (systemd o servicio de usuario):
```bash
# Editar la variable de entorno de Ollama
systemctl --user edit ollama
# O en el archivo de servicio, añadir bajo [Service]:
# Environment="OLLAMA_HOST=0.0.0.0:11434"
# (o si usas un puerto personalizado como 3000):
# Environment="OLLAMA_HOST=0.0.0.0:3000"

systemctl --user restart ollama
```

Verifica tu IP de Tailscale en tu PC ejecutando:
```bash
tailscale ip -4
# Ejemplo: 100.85.20.10
```

Prueba que responda desde tu terminal:
```bash
curl http://100.85.20.10:3000/api/tags
```

---

## 📱 Instalación y Configuración en Android

1. **Instalar el APK**:
   * Copia `app-debug.apk` a tu teléfono e instálalo.
2. **Conceder Permiso de Acceso a Notificaciones**:
   * Abre **Livan AI AutoReply**.
   * En la pestaña **Inicio** o **Ajustes**, pulsa el botón **"Conceder Permiso"**.
   * En la pantalla de ajustes de Android, activa el interruptor para **Livan AI AutoReply Service**.
3. **Desactivar Optimización de Batería**:
   * En la pestaña **Ajustes**, pulsa **"Desactivar límite"** para que el sistema Android no cierre el servicio en segundo plano cuando la pantalla esté apagada.
4. **Configurar el Servidor en la App**:
   * Ve a la pestaña **Servidor**.
   * Ingresa la IP de Tailscale de tu PC (ej: `100.85.20.10`) y el puerto (ej: `3000` u `11434`).
   * Pulsa **"Probar Conexión y Consultar Modelos"**. Si la conexión es exitosa, se mostrará la latencia en milisegundos y podrás seleccionar tu modelo en el desplegable.
5. **Añadir Contactos Autorizados**:
   * Ve a la pestaña **Contactos**.
   * Pulsa el botón **"+"** e introduce el nombre exacto de la persona tal como aparece en las notificaciones de WhatsApp (ej: `Juan Perez` o `Mamá`).
   * Elige la personalidad que tendrá el bot con esa persona.
6. **Activar el Bot**:
   * En la pestaña **Inicio**, activa el interruptor maestro **BOT ACTIVO**.

---

## 📊 Historial y Monitoreo

* En la pestaña **Historial** puedes ver en tiempo real cada mensaje recibido, la respuesta automática generada, el tiempo de respuesta del servidor (latencia en ms) y el estado (Enviado, Límite de velocidad, Error, etc.).
* Puedes filtrar por eventos enviados, errores o filtros, y limpiar el historial cuando desees.

---

## ⚠️ Limitaciones Conocidas

1. **Respuestas solo desde la notificación**: La aplicación aprovecha la acción de respuesta rápida de WhatsApp (`RemoteInput`). Si WhatsApp no muestra el botón de responder en la notificación (por ejemplo, si tienes el chat abierto en primer plano o silenciado sin notificaciones), Android no expondrá la acción y el bot no podrá enviar la respuesta.
2. **Notificaciones de grupos**: Por seguridad y para evitar spam, las notificaciones de grupos son ignoradas automáticamente a menos que actives la casilla *"Permitir responder en grupos"* para ese contacto.
3. **Conexión Tailscale requerida**: Si Tailscale se desconecta o no tiene conexión a internet en el móvil, las peticiones a la IP `100.x.x.x` fallarán por timeout. La app registrará el error en el historial y no enviará mensajes rotos.

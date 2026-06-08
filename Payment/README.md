# Módulo de Pagos — Spring Boot + Mercado Pago

## Arquitectura

Este módulo maneja **dos flujos de dinero** independientes:

| Flujo | Descripción | Destino del dinero |
|---|---|---|
| **Suscripción** | Tenant paga su plan mensual | → Tu cuenta (plataforma) |
| **Venta de tienda** | Comprador paga un producto | → Cuenta del tenant (−1% comisión tuya) |

---

## Requisitos previos

### 1. Cuenta de Mercado Pago (plataforma)
- Crea tu cuenta en https://www.mercadopago.com.co
- Ve a **Tus integraciones → Crear aplicación**
- Activa el modo **Marketplace** en la aplicación
- Anota: `Access Token`, `Client ID`, `Client Secret`

### 2. Variables de entorno
```env
MP_ACCESS_TOKEN=APP_USR-xxxxx-xxxxxx       # Tu access token de plataforma
MP_CLIENT_ID=123456789                      # Tu client ID
MP_CLIENT_SECRET=xxxxxxxxxxxxxxxxxxxxxx     # Tu client secret
MP_WEBHOOK_SECRET=tu_secret_para_webhooks   # Lo configuras en el panel de MP

DB_USERNAME=postgres
DB_PASSWORD=secret
APP_BASE_URL=https://tudominio.com
FRONTEND_URL=https://tudominio.com
```

---

## Flujo 1: Suscripciones (Tenant paga su plan)

```
1. POST /api/payments/subscription
   Body: { tenantId, plan: "BASIC|PRO|ENTERPRISE", payerEmail }
   Response: { checkoutUrl, sandboxCheckoutUrl }

2. Frontend redirige al tenant a checkoutUrl

3. Tenant paga en Mercado Pago

4. MP llama a POST /api/payments/webhook
   → WebhookProcessor activa la TenantSubscription automáticamente

5. GET /api/payments/subscription/{tenantId}/active → true
```

---

## Flujo 2: Venta de tienda con split (Checkout Pro)

**Prerequisito:** El tenant debe tener su cuenta MP conectada (Flujo OAuth).

```
1. POST /api/payments/checkout/pro
   Body: { tenantId, externalReference, items: [...] }
   Response: { checkoutUrl, platformFee (1%), netAmount }

2. Comprador paga en la URL de MP

3. MP distribuye automáticamente:
   - 99% → cuenta del tenant
   - 1%  → tu cuenta (marketplace_fee)

4. MP llama webhook → transacción marcada APPROVED + comisión registrada
```

---

## Flujo 3: Venta con tarjeta directo (Checkout API)

**Prerequisito:** Tenant con OAuth conectado.

El frontend debe incluir MercadoPago.js para tokenizar la tarjeta:

```html
<script src="https://sdk.mercadopago.com/js/v2"></script>
<script>
  const mp = new MercadoPago('TU_PUBLIC_KEY');
  // Crear cardToken con los datos del formulario
  const cardToken = await mp.createCardToken({ ... });
  // Mandar cardToken al backend
</script>
```

```
1. POST /api/payments/checkout/card
   Body: { tenantId, cardToken, amount, paymentMethodId, payer: {...} }
   Response: { status: "approved|rejected|in_process", mpPaymentId }
```

---

## Flujo 4: OAuth — Conectar cuenta MP del tenant

El tenant debe conectar SU cuenta de Mercado Pago para poder recibir pagos:

```
1. GET /api/oauth/connect?tenantId=xxx
   Response: { authorizationUrl }

2. Frontend abre authorizationUrl en nueva ventana/tab

3. Tenant inicia sesión en MP y autoriza

4. MP redirige a GET /api/oauth/callback?code=XXX&state=tenantId
   → access_token guardado en tenant_mp_credentials

5. Frontend recibe redirect a /dashboard/settings/payments?status=connected
```

---

## Endpoints completos

| Método | Endpoint | Descripción |
|---|---|---|
| POST | `/api/payments/subscription` | Crear checkout de suscripción |
| GET | `/api/payments/subscription/{tenantId}` | Consultar suscripción |
| GET | `/api/payments/subscription/{tenantId}/active` | ¿Tiene suscripción activa? |
| POST | `/api/payments/checkout/pro` | Checkout Pro con split |
| POST | `/api/payments/checkout/card` | Pago con tarjeta (Checkout API) |
| POST | `/api/payments/webhook` | Webhook de MP (público) |
| GET | `/api/oauth/connect` | Iniciar flujo OAuth tenant |
| GET | `/api/oauth/callback` | Callback OAuth (público) |
| DELETE | `/api/oauth/disconnect/{tenantId}` | Desconectar cuenta MP |

---

## Planes disponibles

| Plan | Precio (COP) |
|---|---|
| BASIC | $299/mes |
| PRO | $799/mes |
| ENTERPRISE | $1,999/mes |

Modifica los valores en `SubscriptionPlan.java`.

---

## Configurar webhook en el panel de MP

1. Ve a tu aplicación en https://www.mercadopago.com.co/developers/panel
2. → **Webhooks** → Agregar URL
3. URL: `https://tudominio.com/api/payments/webhook`
4. Eventos: `payment`, `subscription_preapproval`
5. Copia el **Signing secret** y ponlo en `MP_WEBHOOK_SECRET`

---

## Notas importantes

- **Idempotencia**: El webhook puede llegar múltiples veces. El `WebhookProcessor` evita reprocesar pagos ya en estado final.
- **Seguridad**: El webhook valida la firma HMAC-SHA256 de MP antes de procesar.
- **Tokens OAuth**: Se renuevan automáticamente cuando están próximos a expirar.
- **Producción**: Cifra el campo `access_token` en `tenant_mp_credentials` (usa AES o Vault).
- **Colombia**: El país está configurado para COP. Ajusta `currency` y el dominio OAuth si usas otro país.

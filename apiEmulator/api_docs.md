# API Documentation

## Overview
This API provides access to device management, allowing users to retrieve and modify device states, modes, and other related information. The API uses SQLite for data storage and supports optional authentication.

## Authentication
- Authentication is **optional** (controlled by `AUTH_ENABLED`).
- If enabled, requests must include:
    - `CF-Access-Client-Id` (Header)
    - `CF-Access-Client-Secret` (Header)

## Endpoints

### Get All Devices
**GET /** or **GET /devices**
- Returns a list of all devices.
- **Response:** JSON array of device objects.

### Get Device by Identifier
**GET /devices/{identifier}**
- Fetches a device by `id` or `hs`.
- **Response:** JSON object with device details.
- **Error:** `404` if device not found.

### Get/Set Device State
**GET /devices/{identifier}/state**
- Fetches the device's current state.

**POST /devices/{identifier}/state**
- Updates the device's state (only if `astroman` is `MANUAL`).
- **Request Body:** `{ "state": "ON" | "OFF" }`
- **Errors:** `400` (Invalid data), `403` (Not in MANUAL mode), `404` (Device not found).

### Get/Set Device Mode (astroman)
**GET /devices/{identifier}/astroman**
- Fetches the `astroman` mode of a device.

**POST /devices/{identifier}/astroman**
- Updates the `astroman` mode.
- **Request Body:** `{ "astroman": "ASTRO" | "MANUAL" }`
- **Errors:** `400` (Invalid data), `404` (Device not found).

### Get Specific Field of a Device
**GET /devices/{identifier}/fields/{field_name}**
- Fetches a specific field value.
- **Errors:** `400` (Invalid field), `404` (Device not found).

### Get Next Astro Time
**GET /devices/{identifier}/nextastro**
- Retrieves the next scheduled astro operation.

### Get Last Astro Time
**GET /devices/{identifier}/lastastro**
- Retrieves the last performed astro operation.

## Error Responses
- `400` - Bad request (invalid data or parameters).
- `401` - Unauthorized (if auth is enabled and credentials are missing or incorrect).
- `403` - Forbidden (operation not allowed based on state).
- `404` - Resource not found.
- `500` - Internal server error.

## Notes
- Database: `styring.db`
- API runs on `0.0.0.0:5053`.
- Ensure the `secret.txt` file exists if authentication is enabled.


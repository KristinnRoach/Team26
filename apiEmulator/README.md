# API Emulator for Gungnir App

This directory contains a Python Flask-based emulator for the backend API used by the main Android application. It simulates the behavior of the device management API described in the Gungnir project.

## Origin

The core emulator logic (`intrapi.py`) is derived from the **Gungnir project** by davidjrb:
[https://github.com/davidjrb/gungnir](https://github.com/davidjrb/gungnir)

## Files

*   **`intrapi.py`**: The main Flask application file that runs the API endpoints. Based on the original Gungnir repo.
*   **`create_db.py`**: A **custom script** (not from the original Gungnir repo). It creates and populates the `styring.db` SQLite database with mock data. The database schema and mock data were reverse-engineered based on the Gungnir project's expected structure to allow `intrapi.py` to function correctly for testing the Android app.
*   **`styring.db`**: (Generated File) The SQLite database file created by `create_db.py` and used by `intrapi.py`.
*   **(Optional) `secret.txt`**: (If used/needed) Contains credentials if API authentication is enabled in `intrapi.py`.

## Setup and Usage

1.  **Prerequisites:** Ensure you have Python 3 installed.
2.  **Install Dependencies:**
    ```bash
    pip install Flask
    ```
3.  **Create Database (Run Once):** Execute the custom script to set up the database.
    ```bash
    python create_db.py
    ```
4.  **Run Emulator:** Start the Flask server.
    ```bash
    python intrapi.py
    ```

The emulator will listen on `0.0.0.0:5053` by default, ready to receive requests from the Android application (running on an emulator or configured to reach this address).
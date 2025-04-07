import sqlite3
import json
import os
import shutil

# Connect to the database
conn = sqlite3.connect('styring.db')
cursor = conn.cursor()

# Fetch all devices from the database
cursor.execute('SELECT id, hs, hverfi, tsip, ds, heimilisfang, lat, lon, telno, comment, '
               'astroman, uxstate, inputstate, outputstate, localremote, astrostate, '
               'nextastrotime, nextastroOP, lastastrotime, lasastroOP, inputpin, outputpin, '
               'created_at, updated_at FROM heimtaugaskapar')
devices = cursor.fetchall()

# Close the database connection
conn.close()

# Create GeoJSON from the database data
geojson = {
    "type": "FeatureCollection",
    "features": []
}

# Create features from devices data
for device in devices:
    id, hs, hverfi, tsip, ds, heimilisfang, lat, lon, telno, comment, \
        astroman, uxstate, inputstate, outputstate, localremote, astrostate, \
        nextastrotime, nextastroOP, lastastrotime, lasastroOP, inputpin, outputpin, \
        created_at, updated_at = device

    # Create a GeoJSON feature for this device
    feature = {
        "type": "Feature",
        "properties": {
            "id": id,
            "stationId": id,            # Match property names used in MaplibreScreen
            "stationName": hs,          # Using hs as station name
            "name": hs,                 # Alternative property name
            "status": outputstate,      # Using outputstate as status
            "controlMode": astroman,    # Using astroman as control mode
            "hverfi": hverfi,           # Additional property
            "address": heimilisfang,    # Additional property
            "lastUpdated": updated_at   # Additional property
        },
        "geometry": {
            "type": "Point",
            "coordinates": [lon, lat]   # GeoJSON uses [longitude, latitude] order
        }
    }

    geojson["features"].append(feature)

# Write GeoJSON to file
geojson_file = "stations.geojson"
with open(geojson_file, 'w') as f:
    json.dump(geojson, f, indent=2)

# Create resource directories if they don't exist
android_assets_dir = "../app/src/main/assets"
os.makedirs(android_assets_dir, exist_ok=True)

# Copy GeoJSON to Android assets directory
assets_geojson_path = os.path.join(android_assets_dir, "stations.geojson")
shutil.copy(geojson_file, assets_geojson_path)

print(f"GeoJSON file created at '{geojson_file}'")
print(f"GeoJSON file copied to Android assets at '{assets_geojson_path}'")
import sqlite3

# Connect to the database
conn = sqlite3.connect('styring.db')
cursor = conn.cursor()

# Drop the table if it exists
cursor.execute('DROP TABLE IF EXISTS heimtaugaskapar')

# Create the heimtaugaskapar table with all columns, including inputpin and outputpin
cursor.execute('''
CREATE TABLE heimtaugaskapar (
    id TEXT PRIMARY KEY,
    hs TEXT,
    hverfi TEXT,
    tsip TEXT,
    ds TEXT,
    heimilisfang TEXT,
    lat REAL,
    lon REAL,
    telno TEXT,
    comment TEXT,
    astroman TEXT,
    uxstate TEXT,
    inputstate TEXT,
    outputstate TEXT,
    localremote TEXT,
    astrostate TEXT,
    nextastrotime TEXT,
    nextastroOP TEXT,
    lastastrotime TEXT,
    lasastroOP TEXT,
    inputpin TEXT,
    outputpin TEXT,
    created_at TEXT,
    updated_at TEXT
)
''')

# Dummy data for 10 devices across 3 groups (hverfi: Miðborg, Vesturbær, Breiðholt)
devices = [
    # Group 1: Miðborg (Downtown)
    ('dev1', 'HS001', 'Miðborg', '192.168.1.101', 'DS001', 'Laugavegur 12, 101 Reykjavík', 64.1465, -21.9426, '+354 1234567', 'Installed 2020',
     'ASTRO', 'ON', 'ON', 'ON', 'REMOTE', 'ON', '2025-03-06T18:00:00.000', 'OFF', '2025-03-05T16:30:00.000', 'ON',
     'D1', 'D2', '2025-03-01T12:00:00.000', '2025-03-05T10:00:00.000'),
    ('dev2', 'HS002', 'Miðborg', '192.168.1.102', 'DS002', 'Hverfisgata 5, 101 Reykjavík', 64.1470, -21.9350, '+354 2345678', 'New LED lights',
     'MANUAL', 'OFF', 'OFF', 'OFF', 'REMOTE', 'OFF', '2025-03-06T07:30:00.000', 'ON', '2025-03-05T18:00:00.000', 'OFF',
     'D3', 'D4', '2025-03-02T13:00:00.000', '2025-03-05T11:00:00.000'),
    ('dev3', 'HS003', 'Miðborg', '192.168.1.103', 'DS003', 'Skólavörðustígur 10, 101 Reykjavík', 64.1450, -21.9300, '+354 3456789', 'Tourist area',
     'MANUAL', 'ON', 'ON', 'OFF', 'LOCAL', 'ON', '2025-03-07T07:45:00.000', 'OFF', '2025-03-05T16:45:00.000', 'ON',
     'D5', 'D6', '2025-03-03T14:00:00.000', '2025-03-05T12:00:00.000'),
    ('dev4', 'HS004', 'Miðborg', '192.168.1.104', 'DS004', 'Austurstræti 1, 101 Reykjavík', 64.1480, -21.9400, '+354 4567890', 'Near harbor',
     'ASTRO', 'ON', 'ON', 'ON', 'REMOTE', 'ON', '2025-03-07T18:15:00.000', 'OFF', '2025-03-05T07:00:00.000', 'ON',
     'D7', 'D8', '2025-03-04T15:00:00.000', '2025-03-05T13:00:00.000'),

    # Group 2: Vesturbær (West Town)
    ('dev5', 'HS005', 'Vesturbær', '192.168.1.105', 'DS005', 'Hringbraut 20, 107 Reykjavík', 64.1420, -21.9500, '+354 5678901', 'Near hospital',
     'ASTRO', 'OFF', 'OFF', 'OFF', 'REMOTE', 'OFF', '2025-03-06T07:15:00.000', 'ON', '2025-03-05T18:30:00.000', 'OFF',
     'D9', 'D10', '2025-03-05T09:00:00.000', '2025-03-05T14:00:00.000'),
    ('dev6', 'HS006', 'Vesturbær', '192.168.1.106', 'DS006', 'Hofsvallagata 15, 107 Reykjavík', 64.1430, -21.9600, '+354 6789012', 'Residential street',
     'MANUAL', 'ON', 'ON', 'ON', 'REMOTE', 'ON', '2025-03-07T18:30:00.000', 'OFF', '2025-03-05T16:15:00.000', 'ON',
     'D11', 'D12', '2025-03-02T10:00:00.000', '2025-03-05T15:00:00.000'),
    ('dev7', 'HS007', 'Vesturbær', '192.168.1.107', 'DS007', 'Melhagi 3, 107 Reykjavík', 64.1400, -21.9550, '+354 7890123', 'Near school',
     'ASTRO', 'OFF', 'OFF', 'OFF', 'REMOTE', 'OFF', '2025-03-06T07:00:00.000', 'ON', '2025-03-05T18:45:00.000', 'OFF',
     'D13', 'D14', '2025-03-04T11:00:00.000', '2025-03-05T16:00:00.000'),

    # Group 3: Breiðholt (South-East Suburb)
    ('dev8', 'HS008', 'Breiðholt', '192.168.1.108', 'DS008', 'Árskógar 10, 109 Reykjavík', 64.1080, -21.8200, '+354 8901234', 'Near playground',
     'MANUAL', 'OFF', 'OFF', 'ON', 'LOCAL', 'OFF', '2025-03-08T07:30:00.000', 'ON', '2025-03-05T16:00:00.000', 'OFF',
     'D15', 'D16', '2025-03-03T12:00:00.000', '2025-03-05T17:00:00.000'),
    ('dev9', 'HS009', 'Breiðholt', '192.168.1.109', 'DS009', 'Breiðholtsbraut 5, 109 Reykjavík', 64.1100, -21.8300, '+354 9012345', 'High traffic',
     'ASTRO', 'ON', 'ON', 'ON', 'REMOTE', 'ON', '2025-03-06T18:45:00.000', 'OFF', '2025-03-05T07:15:00.000', 'ON',
     'D17', 'D18', '2025-03-04T13:00:00.000', '2025-03-05T18:00:00.000'),
    ('dev10', 'HS010', 'Breiðholt', '192.168.1.110', 'DS010', 'Seljabraut 7, 109 Reykjavík', 64.1050, -21.8250, '+354 0123456', 'Residential area',
     'ASTRO', 'OFF', 'OFF', 'OFF', 'REMOTE', 'OFF', '2025-03-07T07:45:00.000', 'ON', '2025-03-05T18:15:00.000', 'OFF',
     'D19', 'D20', '2025-03-05T08:00:00.000', '2025-03-05T19:00:00.000')
]

# Insert dummy data
cursor.executemany('''
INSERT INTO heimtaugaskapar (
    id, hs, hverfi, tsip, ds, heimilisfang, lat, lon, telno, comment, 
    astroman, uxstate, inputstate, outputstate, localremote, astrostate, 
    nextastrotime, nextastroOP, lastastrotime, lasastroOP, inputpin, outputpin, 
    created_at, updated_at
) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
''', devices)

# Commit and close
conn.commit()
conn.close()

print("Database 'styring.db' dropped (if existed), recreated, and populated with 10 mock examples across 3 hverfi groups, including inputpin and outputpin.")
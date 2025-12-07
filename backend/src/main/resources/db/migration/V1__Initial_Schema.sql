
CREATE TABLE platforms (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL UNIQUE,       -- "Steam", "GOG"
    icon_url TEXT,
    type TEXT NOT NULL CHECK (type IN ('API', 'MANUAL')) -- API for online sync and MANUAL for locally added games
);

CREATE TABLE platform_paths (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    platform_id INTEGER NOT NULL,
    path TEXT NOT NULL,

    FOREIGN KEY (platform_id) REFERENCES platforms(id) ON DELETE CASCADE
);

INSERT INTO platforms (name, type) VALUES
('Steam', 'API'),
('Epic', 'API'),
('GOG', 'API'),
('Battle.net', 'API'),
('Itch.io', 'API'),
('Local', 'MANUAL');

INSERT INTO platform_paths (platform_id, path) VALUES
(1, 'C:/Program Files (x86)/Steam'),         -- Steam
(2, 'C:/Program Files/Epic Games'),          -- Epic
(3, 'C:/GOG Games'),                         -- GOG
(4, 'C:/Program Files (x86)/Battle.net'),    -- Battle.net
(5, 'C:/Users/%USERNAME%/AppData/Roaming/itch'); -- Itch

CREATE TABLE games (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    title TEXT NOT NULL,
    igdb_id TEXT UNIQUE,             -- Universal Game ID from IGDB
    cover_url TEXT,
    description TEXT,
    release_date DATE,
    publisher TEXT
    developer TEXT
);

CREATE TABLE library_entries (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    game_id INTEGER NOT NULL,
    platform_id INTEGER NOT NULL,
    is_installed BOOLEAN DEFAULT 0,
    install_path TEXT,
    platform_game_id TEXT,
    playtime_minutes INTEGER DEFAULT 0,
    last_played TIMESTAMP,

    FOREIGN KEY (game_id) REFERENCES games(id),
    FOREIGN KEY (platform_id) REFERENCES platforms(id),
    UNIQUE(game_id, platform_id)     -- Prevent duplicate rows for the same ownership
);

CREATE TABLE tags (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL UNIQUE,
    color_hex TEXT
);

CREATE TABLE game_tags (
    game_id INTEGER NOT NULL,
    tag_id INTEGER NOT NULL,

    PRIMARY KEY (game_id, tag_id),
    FOREIGN KEY (game_id) REFERENCES games(id) ON DELETE CASCADE,
    FOREIGN KEY (tag_id) REFERENCES tags(id) ON DELETE CASCADE
);
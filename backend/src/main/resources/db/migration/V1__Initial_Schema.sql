
CREATE TABLE platforms (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL UNIQUE,
    icon_url TEXT,
    executable_path TEXT,
    manifests_path TEXT,

    type TEXT NOT NULL CHECK (type IN ('API', 'MANUAL')) -- API for online sync and MANUAL for locally added games
);

CREATE TABLE platform_paths (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    platform_id INTEGER NOT NULL,
    path TEXT NOT NULL,

    FOREIGN KEY (platform_id) REFERENCES platforms(id) ON DELETE CASCADE
);

INSERT INTO platforms (name, type, executable_path) VALUES
('Steam', 'API', 'C:/Program Files (x86)/Steam/steam.exe'),
('Epic', 'API', 'C:/Program Files (x86)/Epic/epic.exe'),
('GOG', 'API', 'C:/Program Files (x86)/GOG/gog.exe');

INSERT INTO platforms (name, type, executable_path, manifests_path) VALUES
('Epic', 'API', 'C:/Program Files (x86)/Epic/epic.exe', 'C:\ProgramData\Epic\EpicGamesLauncher\Data\Manifests');

INSERT INTO platform_paths (platform_id, path) VALUES
(1, 'C:/Program Files (x86)/Steam'),         -- Steam
(1, 'G:/Play/Steam'),                        -- Steam
(2, 'C:/Program Files/Epic Games'),          -- Epic
(3, 'C:/GOG Games');                         -- GOG

CREATE TABLE games (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    title TEXT NOT NULL,
    igdb_id TEXT UNIQUE,
    cover_url TEXT,
    description TEXT,
    release_date DATE,
    publisher TEXT,
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
    game_size BIGINT,

    FOREIGN KEY (game_id) REFERENCES games(id),
    FOREIGN KEY (platform_id) REFERENCES platforms(id),
    UNIQUE(game_id, platform_id)
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
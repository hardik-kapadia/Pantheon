CREATE TABLE platforms (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    platform_name TEXT NOT NULL UNIQUE,
    icon_url TEXT,
);

CREATE TABLE platform_local_configs (
    if INTEGER PRIMARY KEY AUTOINCREMENT,
    platform_id INTEGER NOT NULL,
    executable_path TEXT,
    manifests_path TEXT,
    local_scan_strategy TEXT CHECK (scan_strategy IN ('LIBRARY', 'MANIFEST')),

    FOREIGN KEY (platform_id) REFERENCES platforms(id),
    CONSTRAINT path_exists CHECK ((local_scan_strategy = 'LIBRARY' AND executable_path IS NOT NULL) OR (local_scan_strategy = 'MANIFEST' AND manifests_path IS NOT NULL))
);

INSERT INTO platforms (platform_name, executable_path) VALUES
('Steam', 'C:/Program Files (x86)/Steam/steam.exe'),
('GOG', 'C:/Program Files (x86)/GOG/gog.exe');

INSERT INTO platforms (name, type, executable_path, manifests_path, scan_strategy) VALUES
('Epic', 'C:/Program Files (x86)/Epic/epic.exe', 'C:\ProgramData\Epic\EpicGamesLauncher\Data\Manifests', 'MANIFEST');

CREATE TABLE libraries (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    platform_id INTEGER NOT NULL,
    library_path TEXT,
    is_global BOOLEAN NOT NULL DEFAULT 0,
    is_accessible BOOLEAN DEFAULT 1,
    last_scanned TIMESTAMP,

    FOREIGN KEY (platform_id) REFERENCES platforms(id),
    CONSTRAINT path_required_if_not_global CHECK ((is_global = 1) OR (is_global = 0 AND library_path IS NOT NULL))
);

INSERT INTO libraries (platform_id, path) VALUES
(1, 'C:/Program Files (x86)/Steam'),
(1, 'G:/Play/Steam'),
(2, 'C:/GOG Games');

INSERT INTO libraries (platform_id, is_global) VALUES
(3, 1);

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

CREATE TABLE remote_entitlements (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    game_id INTEGER NOT NULL,
    platform_id INTEGER NOT NULL,
    platform_game_id TEXT NOT NULL,
    playtime_minutes INTEGER NOT NULL DEFAULT 0,

    FOREIGN KEY (game_id) REFERENCES games(id),
    FOREIGN KEY (platform_id) REFERENCES platforms(id),
    UNIQUE(platform_id, platform_game_id)
);

CREATE TABLE local_installations (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    entitlement_id INTEGER NOT NULL UNIQUE,
    library_id INTEGER NOT NULL,
    install_folder TEXT NOT NULL,
    executable_path TEXT NOT NULL,
    game_size_bytes BIGINT DEFAULT 0,
    is_valid BOOLEAN DEFAULT 1,
    last_played TIMESTAMP,

    FOREIGN KEY (entitlement_id) REFERENCES remote_entitlements(id) ON DELETE CASCADE,
    FOREIGN KEY (library_id) REFERENCES libraries(id) ON DELETE CASCADE,
    UNIQUE(entitlement_id)
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

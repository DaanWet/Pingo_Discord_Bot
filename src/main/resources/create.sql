CREATE TABLE IF NOT EXISTS Record
(
    ID INT,
    Type  VARCHAR(50) NOT NULL PRIMARY KEY,
    IsInt BOOLEAN DEFAULT TRUE
);
CREATE TABLE IF NOT EXISTS Member
(
    UserId     BIGINT NOT NULL,
    GuildId    BIGINT NOT NULL,
    Credits    INT DEFAULT 0,
    LastDaily  TIMESTAMP,
    LastWeekly TIMESTAMP,
    Experience INT DEFAULT 0,
    CurrentStreak  INT DEFAULT 0,
    PRIMARY KEY (UserId, GuildId)
);
CREATE TABLE IF NOT EXISTS RoleAssign
(
    Name      VARCHAR(255) NOT NULL,
    GuildId   BIGINT       NOT NULL,
    ChannelId BIGINT,
    MessageId BIGINT,
    Sorting VARCHAR(255) DEFAULT 'NONE', Compacting VARCHAR(20) DEFAULT 'NORMAL', Title VARCHAR(255), PRIMARY KEY(Name, GuildId)
);
CREATE TABLE IF NOT EXISTS Role
(
    RoleId  BIGINT       NOT NULL,
    Name    VARCHAR(255) NOT NULL,
    Emoji   VARCHAR(255) NOT NULL,
    Type    VARCHAR(255) NOT NULL,
    GuildId BIGINT       NOT NULL,
    FOREIGN KEY (Type, GuildId) REFERENCES RoleAssign (Name, GuildId),
    PRIMARY KEY (Emoji, Type, GuildId)
);
CREATE TABLE IF NOT EXISTS UserRecord
(
    UserId  BIGINT      NOT NULL,
    GuildId BIGINT      NOT NULL,
    Name    VARCHAR(50) NOT NULL,
    Link    VARCHAR(255),
    Value   DOUBLE      NOT NULL,
    PRIMARY KEY (UserId, GuildId, Name),
    FOREIGN KEY (UserId, GuildId) REFERENCES Member (UserId, GuildId),
    FOREIGN KEY (Name) REFERENCES Record (Type)
);
CREATE TABLE IF NOT EXISTS Setting
(
    ID        INT AUTO_INCREMENT PRIMARY KEY,
    Name      VARCHAR(50) NOT NULL,
    ValueType VARCHAR(10) NOT NULL,
    Type      VARCHAR(50),
    Multiple  BOOLEAN     NOT NULL,
    UNIQUE (Name, Type)
);
CREATE TABLE IF NOT EXISTS GuildSetting
(
    GuildId BIGINT       NOT NULL,
    ID      INT          NOT NULL,
    Value   VARCHAR(255) NOT NULL,
    Type    VARCHAR(50),
    FOREIGN KEY (ID) REFERENCES Setting (ID),
    PRIMARY KEY (GuildId, ID, Value)
);
CREATE TABLE IF NOT EXISTS Cooldown
(
    GuildId BIGINT NOT NULL,
    UserId  BIGINT NOT NULL,
    Setting INT    NOT NULL,
    Time    TIMESTAMP,
    PRIMARY KEY (GuildId, UserId, Setting),
    FOREIGN KEY (Setting) REFERENCES Setting (ID)
);
CREATE TABLE IF NOT EXISTS UserAchievement
(
    GuildId BIGINT NOT NULL,
    UserId BIGINT NOT NULL,
    Achievement VARCHAR(50),
    Achieved BOOLEAN DEFAULT FALSE,
    Time TIMESTAMP,
    PRIMARY KEY (GuildId, UserId, Achievement)
);
CREATE TABLE IF NOT EXISTS {LastKnownName}
(
   uuid VARCHAR (36) NOT NULL,
   lastKnownName VARCHAR (16),
   PRIMARY KEY (uuid)
);
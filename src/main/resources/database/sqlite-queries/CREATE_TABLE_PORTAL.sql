CREATE TABLE IF NOT EXISTS {Portal}
(
   name NVARCHAR (180) NOT NULL,
   network NVARCHAR (180) NOT NULL,
   destination NVARCHAR (180),
   world NVARCHAR (255) NOT NULL,
   x INTEGER,
   y INTEGER,
   z INTEGER,
   ownerUUID VARCHAR (36),
   gateFileName NVARCHAR (255),
   facing INTEGER,
   flipZ BOOLEAN,
   metaData TEXT,
   PRIMARY KEY
   (
      name,
      network
   )
);
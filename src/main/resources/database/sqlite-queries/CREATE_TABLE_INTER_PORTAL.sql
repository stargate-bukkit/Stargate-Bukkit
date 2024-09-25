CREATE TABLE IF NOT EXISTS {InterPortal}
(
   name NVARCHAR (180) NOT NULL,
   network NVARCHAR (180) NOT NULL,
   destination NVARCHAR (180),
   world NVARCHAR (36) NOT NULL,
   x INTEGER,
   y INTEGER,
   z INTEGER,
   ownerUUID VARCHAR (36),
   gateFileName NVARCHAR (255),
   facing INTEGER,
   flipZ BOOLEAN,
   homeServerId VARCHAR (36),
   metaData TEXT,
   PRIMARY KEY
   (
      name,
      network
   )
);
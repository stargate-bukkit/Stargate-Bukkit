CREATE TABLE IF NOT EXISTS {PortalPosition}
(
   portalName NVARCHAR (180) NOT NULL,
   networkName NVARCHAR (180) NOT NULL,
   xCoordinate INTEGER NOT NULL,
   yCoordinate INTEGER NOT NULL,
   zCoordinate INTEGER NOT NULL,
   positionType INTEGER NOT NULL,
   metaData TEXT,
   pluginName VARCHAR(255) DEFAULT "Stargate",
   PRIMARY KEY
   (
      portalName,
      networkName,
      xCoordinate,
      yCoordinate,
      zCoordinate
   ),
   FOREIGN KEY
   (
      portalName,
      networkName
   )
   REFERENCES {Portal}
   (
      name,
      network
   )
   ON UPDATE CASCADE,
   FOREIGN KEY (positionType) REFERENCES {PositionType} (id)
);
CREATE TABLE IF NOT EXISTS {PortalPosition}1
(
   portalName NVARCHAR (180) NOT NULL,
   networkName NVARCHAR (180) NOT NULL,
   xCoordinate INTEGER NOT NULL,
   yCoordinate INTEGER NOT NULL,
   zCoordinate INTEGER NOT NULL,
   positionType INTEGER NOT NULL,
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
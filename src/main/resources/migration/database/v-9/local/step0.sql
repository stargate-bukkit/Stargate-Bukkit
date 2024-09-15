/*
 * Add a update on cascade constraint to the PortalPosition table
 * This is the only way of doing it in sqlite (by recreating the table)
 */

CREATE TABLE IF NOT EXISTS {PortalPosition}1
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
   ON UPDATE CASCADE
   ON DELETE CASCADE,
   FOREIGN KEY (positionType) REFERENCES {PositionType} (id)
);

INSERT INTO {PortalPosition}1 SELECT *
FROM
   {PortalPosition};

DROP TABLE {PortalPosition};

ALTER TABLE {PortalPosition}1 RENAME TO {PortalPosition};
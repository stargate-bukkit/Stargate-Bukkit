/*
 * Add a update on cascade constraint to the InterPortalPosition table
 * This is the only way of doing it in sqlite (by recreating the table)
 */


CREATE TABLE IF NOT EXISTS {InterPortalPosition}1
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
   REFERENCES {InterPortal}
   (
      name,
      network
   )
   ON UPDATE CASCADE,
   FOREIGN KEY (positionType) REFERENCES {PositionType} (id)
);

INSERT INTO {InterPortalPosition}1 SELECT
   portalName,
   networkName,
   xCoordinate,
   yCoordinate,
   zCoordinate,
   positionType
FROM
   {InterPortalPosition};
   
DROP TABLE {InterPortalPosition};

ALTER TABLE {InterPortalPosition}1 RENAME TO {InterPortalPosition};
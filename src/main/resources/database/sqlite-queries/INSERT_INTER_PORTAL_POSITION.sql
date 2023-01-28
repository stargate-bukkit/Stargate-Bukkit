INSERT INTO {InterPortalPosition}
(
   portalName,
   networkName,
   xCoordinate,
   yCoordinate,
   zCoordinate,
   positionType
)
VALUES
(
   ?,
   ?,
   ?,
   ?,
   ?,

   (
      SELECT
         {PositionType}.id
      FROM
         {PositionType}
      WHERE
         {PositionType}.positionName = ?
   )
);
SELECT *,

   (
      SELECT
         {PositionType}.positionName
      FROM
         {PositionType}
      WHERE
         {PositionType}.id = positionType
   ) as
positionName
FROM
   {PortalPosition}
WHERE
   networkName = ?
AND
   portalName = ?;
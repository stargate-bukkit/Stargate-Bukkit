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
   {InterPortalPosition}
WHERE
   networkName = ?
AND
   portalName = ?;
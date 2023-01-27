DELETE
FROM
   {InterPortalPosition}
WHERE
   portalName = ?
AND
   networkName = ?
AND
   xCoordinate = ?
AND
   yCoordinate = ?
AND
   zCoordinate = ?;
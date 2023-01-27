SELECT *
FROM
   sqlite_master
WHERE
   type = 'index'
AND
   name = '{InterPortalPositionIndex}';
INSERT INTO {PortalFlagRelation}
(
   name,
   network,
   flag
)
VALUES
(
   ?,
   ?,

   (
      SELECT
         id
      FROM
         {Flag}
      WHERE
         `character` = ?
   )
);
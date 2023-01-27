INSERT INTO {InterPortalFlagRelation}
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
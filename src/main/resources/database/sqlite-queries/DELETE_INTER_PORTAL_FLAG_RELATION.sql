DELETE
FROM
   {InterPortalFlagRelation}
WHERE
   name = ?
AND
   network = ?
AND
   flag =
(
   SELECT
      id
   FROM
      {Flag}
   WHERE
      `character` = ?
);
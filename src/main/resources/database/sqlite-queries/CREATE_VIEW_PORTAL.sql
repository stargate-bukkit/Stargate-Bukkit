CREATE VIEW IF NOT EXISTS {PortalView} AS
SELECT
   {Portal}.*,
   COALESCE

   (
      GROUP_CONCAT
      (
         {Flag}.`character`,
         ''
      ),
      ''
   ) AS flags,
   {LastKnownName}.lastKnownName
FROM
   {Portal}
LEFT
OUTER
JOIN {PortalFlagRelation} ON {Portal}.name = {PortalFlagRelation}.name
AND
   {Portal}.network = {PortalFlagRelation}.network
LEFT
OUTER
JOIN {Flag} ON {PortalFlagRelation}.flag = {Flag}.id
LEFT
OUTER
JOIN {LastKnownName} ON {Portal}.network = {LastKnownName}.uuid
GROUP BY
   {Portal}.name,
{Portal}.network;
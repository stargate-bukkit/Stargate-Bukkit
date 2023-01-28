CREATE VIEW IF NOT EXISTS {InterPortalView} AS
SELECT
   {InterPortal}.*,
   COALESCE

   (
      GROUP_CONCAT
      (
         {Flag}.`character`,
         ''
      ),
      ''
   ) AS flags,
   {LastKnownName}.lastKnownName,
   {ServerInfo}.serverName
FROM
   {InterPortal}
LEFT
OUTER
JOIN {InterPortalFlagRelation} ON {InterPortal}.name = {InterPortalFlagRelation}.name
AND
   {InterPortal}.network = {InterPortalFlagRelation}.network
LEFT
OUTER
JOIN {Flag} ON {InterPortalFlagRelation}.flag = {Flag}.id
LEFT
OUTER
JOIN {LastKnownName} ON {InterPortal}.network = {LastKnownName}.uuid
LEFT
OUTER
JOIN {ServerInfo} ON {ServerInfo}.serverId = {InterPortal}.homeServerId
GROUP BY
   {InterPortal}.name,
{InterPortal}.network;
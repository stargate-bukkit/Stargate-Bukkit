/*
 * Add a update on cascade constraint to the InterPortalFlagRelation table
 * This is the only way of doing it in sqlite (by recreating the table)
 */

CREATE TABLE IF NOT EXISTS {InterPortalFlagRelation}1
(
   name NVARCHAR (180) NOT NULL,
   network NVARCHAR (180) NOT NULL,
   flag INTEGER NOT NULL,
   PRIMARY KEY
   (
      name,
      network,
      flag
   ),
   FOREIGN KEY
   (
      name,
      network
   )
   REFERENCES {InterPortal}
   (
      name,
      network
   )
   ON UPDATE CASCADE
   ON DELETE CASCADE,
   FOREIGN KEY (flag) REFERENCES {Flag} (id)
);

INSERT INTO {InterPortalFlagRelation}1 SELECT *
FROM
   {InterPortalFlagRelation};
   
DROP VIEW {InterPortalView};

DROP TABLE {InterPortalFlagRelation};

ALTER TABLE {InterPortalFlagRelation}1 RENAME TO {InterPortalFlagRelation};

CREATE_VIEW_INTER_PORTAL;
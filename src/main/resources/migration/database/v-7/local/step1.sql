/*
 * Add a update on cascade constraint to the InterPortalFlagRelation table
 * This is the only way of doing it in sqlite (by recreating the table)
 */

CREATE TABLE IF NOT EXISTS {PortalFlagRelation}1
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
   REFERENCES {Portal}
   (
      name,
      network
   )
   ON UPDATE CASCADE,
   FOREIGN KEY (flag) REFERENCES {Flag} (id)
);

INSERT INTO {PortalFlagRelation}1 SELECT *
FROM
   {PortalFlagRelation};
   
DROP VIEW {PortalView};

DROP TABLE {PortalFlagRelation};

ALTER TABLE {PortalFlagRelation}1 RENAME TO {PortalFlagRelation};

CREATE_VIEW_PORTAL;
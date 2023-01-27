CREATE TABLE IF NOT EXISTS {InterPortalFlagRelation}
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
   ON UPDATE CASCADE,
   FOREIGN KEY (flag) REFERENCES {Flag} (id)
);
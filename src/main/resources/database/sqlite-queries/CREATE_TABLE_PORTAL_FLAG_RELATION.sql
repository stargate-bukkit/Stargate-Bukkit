CREATE TABLE IF NOT EXISTS {PortalFlagRelation}
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
   ON UPDATE CASCADE
   ON DELETE CASCADE,
   FOREIGN KEY (flag) REFERENCES {Flag} (id)
);
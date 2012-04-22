CREATE TABLE doclogrecord (
  id bigint NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
  url long varchar NOT NULL,
  ip varchar(255) DEFAULT NULL,
  proxyIp varchar(255) DEFAULT NULL,
  action varchar(255) NOT NULL,
  actionParam long varchar DEFAULT NULL,
  dateTime char(29) NOT NULL,
  x int DEFAULT NULL,
  y int DEFAULT NULL,
  w int DEFAULT NULL,
  h int DEFAULT NULL,
  PRIMARY KEY (id)
)
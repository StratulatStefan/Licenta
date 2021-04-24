CREATE TABLE user_type (
	user_type          VARCHAR(15) PRIMARY KEY,
	replication_factor         INT NOT NULL,
	available_storage       BIGINT NOT NULL
);


CREATE TABLE user(
	id                       INT NOT NULL AUTO_INCREMENT,
	name             VARCHAR(50) NOT NULL,
	email            VARCHAR(30) NOT NULL,
	password         VARCHAR(30) NOT NULL,
	type             VARCHAR(15) NOT NULL,
	storage_quantity      BIGINT NOT NULL,
	country           VARCHAR(20) NOT NULL,

	PRIMARY KEY (id),
	FOREIGN KEY (type) REFERENCES user_type(user_type)
);

CREATE TABLE power_status(
	status VARCHAR(10),

	PRIMARY KEY(status)
);

INSERT INTO power_status VALUES ("ON");
INSERT INTO power_status VALUES ("OFF");


CREATE TABLE log_type(
	type VARCHAR(20),

	PRIMARY KEY(type)
);


INSERT INTO log_type VALUES("SUCCESS");
INSERT INTO log_type VALUES("WARNING");
INSERT INTO log_type VALUES("ERROR");

CREATE TABLE internal_node(
	ip_address        VARCHAR(19),
	location_country  VARCHAR(20) NOT NULL,
	status            VARCHAR(10) NOT NULL,

	PRIMARY KEY (ip_address),
	FOREIGN KEY (status) REFERENCES power_status(status)
);


CREATE TABLE log(
	registerId     INT AUTO_INCREMENT,
	node_address   VARCHAR(19)  NOT NULL,
	message_type   VARCHAR(20)  NOT NULL,
	description    VARCHAR(100) NOT NULL,
	register_date  TIMESTAMP    NOT NULL,

	PRIMARY KEY(registerId),
	FOREIGN KEY (node_address) REFERENCES internal_node(ip_address),
	FOREIGN KEY (message_type) REFERENCES log_type(type)
);
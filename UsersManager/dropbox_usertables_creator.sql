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
	county           VARCHAR(20) NOT NULL,
	type             VARCHAR(15) NOT NULL,
	storage_quantity      BIGINT NOT NULL,

	PRIMARY KEY(id),
	FOREIGN KEY (type) REFERENCES user_type(user_type)
);

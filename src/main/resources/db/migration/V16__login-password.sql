alter table login
    add column password varchar(255);

create unique index uq__login_username on login (lower(username)) where username is not null;

create sequence login_local_id_seq start with 1000000000;

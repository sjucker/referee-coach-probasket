create table login
(
    id                  bigserial
        constraint pk__login primary key,
    basketplan_username varchar(255) not null
        constraint uq__login_username unique,
    firstname           varchar(255) not null,
    lastname            varchar(255) not null,
    email               varchar(255) not null
);

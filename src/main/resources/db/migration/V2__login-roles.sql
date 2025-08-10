alter table login
    add column last_login timestamp;

alter table login
    add column referee_coach boolean default false;
alter table login
    add column referee boolean default false;
alter table login
    add column trainer_coach boolean default false;
alter table login
    add column trainer boolean default false;
alter table login
    add column admin boolean default false;

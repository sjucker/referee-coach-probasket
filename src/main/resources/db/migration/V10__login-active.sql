alter table login
    drop column basketplan_username;

alter table login
    add column active boolean default true;

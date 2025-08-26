alter table login
    alter column id drop default;

drop sequence login_id_seq;

create table report
(
    id                 bigserial
        constraint pk__report primary key,
    external_id        varchar(255) not null
        constraint uq__report_external_id unique,
    report_type        varchar(255) not null,
    -- nullable for game-discussions
    coach_id           bigint,
    coach_name         varchar(255),
    reportee_id        bigint,
    reportee_name      varchar(255),
    game_number        varchar(255) not null,
    game_competition   varchar(255) not null,
    game_date          date         not null,
    game_result        varchar(255) not null,
    game_home_team     varchar(255) not null,
    game_home_team_id  integer      not null,
    game_guest_team    varchar(255) not null,
    game_guest_team_id integer      not null,
    game_referee1_id   bigint,
    game_referee1_name varchar(255),
    game_referee2_id   bigint,
    game_referee2_name varchar(255),
    game_referee3_id   bigint,
    game_referee3_name varchar(255),
    game_video_url     varchar(255),
    overall_score      numeric(2, 1),
    created_at         timestamp    not null,
    created_by         bigint       not null,
    updated_at         timestamp    not null,
    updated_by         bigint       not null,
    finished_at        timestamp,
    finished_by        bigint,
    reminder_sent      boolean      not null default false,

    constraint fk__report_coach foreign key (coach_id) references login (id),
    constraint fk__report_reportee foreign key (reportee_id) references login (id),
    constraint fk__report_referee1 foreign key (game_referee1_id) references login (id),
    constraint fk__report_referee2 foreign key (game_referee2_id) references login (id),
    constraint fk__report_referee3 foreign key (game_referee3_id) references login (id),
    constraint fk__report_created_by foreign key (created_by) references login (id),
    constraint fk__report_updated_by foreign key (updated_by) references login (id),
    constraint fk__report_finished_by foreign key (finished_by) references login (id)
);

create table report_comment
(
    id        bigserial
        constraint pk__report_comment primary key,
    report_id bigint       not null,
    type      varchar(255) not null,
    comment   text,
    score     numeric(2, 1),

    constraint fk__report_comment_report foreign key (report_id) references report (id) on delete cascade
);

create table report_criteria
(
    id                bigserial
        constraint pk__report_criteria primary key,
    report_comment_id bigint       not null,
    type              varchar(255) not null,
    comment           text,
    state             varchar(255),

    constraint fk__report_criteria_report foreign key (report_comment_id) references report_comment (id) on delete cascade
);

create table report_video_comment
(
    id                   bigserial
        constraint pk__report_video_comment primary key,
    report_id            bigint    not null,
    timestamp_in_seconds bigint    not null,
    comment              text      not null,
    created_at           timestamp not null,
    created_by           bigint    not null,
    requires_reply       boolean   not null default false,

    constraint fk__report_video_comment_report foreign key (report_id) references report (id) on delete cascade,
    constraint fk__report_video_comment_created_by foreign key (created_by) references login (id) on delete cascade
);

create table report_video_comment_reply
(
    id                      bigserial
        constraint pk__report_video_comment_reply primary key,
    report_video_comment_id bigint    not null,
    reply                   text      not null,
    created_at              timestamp not null,
    created_by              bigint    not null,

    constraint fk__report_video_comment_reply_comment foreign key (report_video_comment_id) references report_video_comment (id) on delete cascade,
    constraint fk__report_video_comment_reply_created_by foreign key (created_by) references login (id) on delete cascade
);

create table report_video_comment_tag
(
    report_video_comment_id bigint not null,
    tag_id                  bigint not null,

    constraint pk__report_video_comment_tag primary key (report_video_comment_id, tag_id),
    constraint fk__report_video_comment_tag_comment foreign key (report_video_comment_id) references report_video_comment (id) on delete cascade,
    constraint fk__report_video_comment_tag_tag foreign key (tag_id) references tag (id) on delete cascade
);

create table report_last_read
(
    report_id    bigint    not null,
    user_id      bigint    not null,
    last_read_at timestamp not null,

    constraint pk__report_comments_last_read primary key (report_id, user_id)
)

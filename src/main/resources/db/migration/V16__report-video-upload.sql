create table report_video_upload
(
    id           bigserial
        constraint pk__report_video_upload primary key,
    object_key   varchar(512) not null,
    filename     varchar(255) not null,
    content_type varchar(255) not null,
    size_bytes   bigint       not null,
    uploaded     boolean      not null default false,
    created_at   timestamp    not null,
    created_by   bigint       not null,

    constraint fk__report_video_upload_created_by foreign key (created_by) references login (id) on delete cascade
);

alter table report_video_comment
    add column report_video_upload_id bigint,
    add constraint fk__report_video_comment_upload foreign key (report_video_upload_id) references report_video_upload (id);

-- snippet comments carry their own uploaded clip and have no timestamp into a full-game video
alter table report_video_comment
    alter column timestamp_in_seconds drop not null;

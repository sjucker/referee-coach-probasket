create table report_video_comment_ref
(
    report_id               bigint not null,
    report_video_comment_id bigint not null,

    constraint pk__report_video_comment_ref primary key (report_id, report_video_comment_id),
    constraint fk__report_coach foreign key (report_id) references report (id) on delete cascade,
    constraint fk__report_reportee foreign key (report_video_comment_id) references report_video_comment (id) on delete cascade
);

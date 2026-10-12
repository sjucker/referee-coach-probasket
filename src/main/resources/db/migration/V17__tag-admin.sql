-- the initial tags were inserted with explicit ids, so the sequence never advanced
select setval(pg_get_serial_sequence('tag', 'id'), coalesce((select max(id) from tag), 0) + 1, false);

-- referenced tags must not be deleted (previously the references were silently cascaded away)
alter table report_video_comment_tag
    drop constraint fk__report_video_comment_tag_tag;

alter table report_video_comment_tag
    add constraint fk__report_video_comment_tag_tag foreign key (tag_id) references tag (id) on delete restrict;

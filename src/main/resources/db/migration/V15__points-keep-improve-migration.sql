insert into report_criteria (report_comment_id, type, state)
select id, 'KEEP_IMAGE', 'FALSE'
from report_comment
where type = 'POINTS_TO_KEEP';

insert into report_criteria (report_comment_id, type, state)
select id, 'KEEP_FOULS', 'FALSE'
from report_comment
where type = 'POINTS_TO_KEEP';

insert into report_criteria (report_comment_id, type, state)
select id, 'KEEP_VIOLATIONS', 'FALSE'
from report_comment
where type = 'POINTS_TO_KEEP';

insert into report_criteria (report_comment_id, type, state)
select id, 'KEEP_MECHANICS', 'FALSE'
from report_comment
where type = 'POINTS_TO_KEEP';

insert into report_criteria (report_comment_id, type, state)
select id, 'KEEP_FITNESS', 'FALSE'
from report_comment
where type = 'POINTS_TO_KEEP';

insert into report_criteria (report_comment_id, type, state)
select id, 'KEEP_GAME_CONTROL', 'FALSE'
from report_comment
where type = 'POINTS_TO_KEEP';


insert into report_criteria (report_comment_id, type, state)
select id, 'IMPROVE_IMAGE', 'FALSE'
from report_comment
where type = 'POINTS_TO_IMPROVE';

insert into report_criteria (report_comment_id, type, state)
select id, 'IMPROVE_FOULS', 'FALSE'
from report_comment
where type = 'POINTS_TO_IMPROVE';

insert into report_criteria (report_comment_id, type, state)
select id, 'IMPROVE_VIOLATIONS', 'FALSE'
from report_comment
where type = 'POINTS_TO_IMPROVE';

insert into report_criteria (report_comment_id, type, state)
select id, 'IMPROVE_MECHANICS', 'FALSE'
from report_comment
where type = 'POINTS_TO_IMPROVE';

insert into report_criteria (report_comment_id, type, state)
select id, 'IMPROVE_FITNESS', 'FALSE'
from report_comment
where type = 'POINTS_TO_IMPROVE';

insert into report_criteria (report_comment_id, type, state)
select id, 'IMPROVE_GAME_CONTROL', 'FALSE'
from report_comment
where type = 'POINTS_TO_IMPROVE';


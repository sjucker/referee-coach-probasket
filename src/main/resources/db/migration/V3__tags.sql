create table tags
(
    id   bigserial
        constraint pk__tags primary key,
    name varchar(255) not null
);

INSERT INTO tags (id, name)
VALUES (1, 'TRAIL');
INSERT INTO tags (id, name)
VALUES (2, 'CENTER');
INSERT INTO tags (id, name)
VALUES (3, 'LEAD');
INSERT INTO tags (id, name)
VALUES (4, 'TV - TRAVELLING');
INSERT INTO tags (id, name)
VALUES (5, 'DD - DOUBLE DRIBBLE');
INSERT INTO tags (id, name)
VALUES (6, 'BCV - BACKCOURT');
INSERT INTO tags (id, name)
VALUES (7, 'TIME V. - TIME 3/5/8');
INSERT INTO tags (id, name)
VALUES (8, 'SCV - SHOT CLOCK VIOLATION');
INSERT INTO tags (id, name)
VALUES (9, 'OOB_T-IN - OUT OF BOUNDS & THROW-IN');
INSERT INTO tags (id, name)
VALUES (10, 'GT_BI - GOAL TENDING AND INTERFERENCE');
INSERT INTO tags (id, name)
VALUES (11, 'UF - UNSPORTSMANLIKE FOUL');
INSERT INTO tags (id, name)
VALUES (12, 'FK - FAKE BEING FOULED');
INSERT INTO tags (id, name)
VALUES (13, 'AOS D - AOS: DEFENDER');
INSERT INTO tags (id, name)
VALUES (14, 'AOS S - AOS: SHOOTER');
INSERT INTO tags (id, name)
VALUES (15, 'NL_CH - BLOCK-CHARGE');
INSERT INTO tags (id, name)
VALUES (16, 'CYL - CYLINDER PLAY');
INSERT INTO tags (id, name)
VALUES (17, 'SCR - SCREENS');
INSERT INTO tags (id, name)
VALUES (18, 'PNR - PICK AND ROLL');
INSERT INTO tags (id, name)
VALUES (19, 'IUA_DEF - USE OH HANDS/ARMS DEFENSIVE');
INSERT INTO tags (id, name)
VALUES (20, 'IUA_OF - USE OF HANDS/ARMS OFFENSIVE');
INSERT INTO tags (id, name)
VALUES (21, 'SHOTS - MOVING SHOTS (AOS or not)');
INSERT INTO tags (id, name)
VALUES (22, 'MC - MARGINAL CALLS');
INSERT INTO tags (id, name)
VALUES (23, 'OP - OBVIOUS PLAYS');
INSERT INTO tags (id, name)
VALUES (24, 'RB - REBOUNDING');
INSERT INTO tags (id, name)
VALUES (25, 'TRANS - TRANSITION');
INSERT INTO tags (id, name)
VALUES (26, 'PLAYER - PLAYER MANAGEMENT');
INSERT INTO tags (id, name)
VALUES (27, 'COACH - COACH MANAGEMENT');
INSERT INTO tags (id, name)
VALUES (28, 'TW - TEAMWORK');
INSERT INTO tags (id, name)
VALUES (29, 'DW - DOUBLE WHISTLE');
INSERT INTO tags (id, name)
VALUES (30, 'COM - COMMUNICATION WITH COLLEAGUES');
INSERT INTO tags (id, name)
VALUES (31, 'PPL - PROCESS THE PLAY');
INSERT INTO tags (id, name)
VALUES (32, 'PW_CW - PATIENT WHISTLE /CADENCED WHISTLE');
INSERT INTO tags (id, name)
VALUES (33, 'QW - QUICK WHISTLE');
INSERT INTO tags (id, name)
VALUES (34, 'OA_CA - OPEN ANGLE vs CLOSE ANGLE');
INSERT INTO tags (id, name)
VALUES (35, 'D&S - DISTANCE AND STATIONARY');
INSERT INTO tags (id, name)
VALUES (36, 'CLOCKS - CLOCKS CONTROL');
INSERT INTO tags (id, name)
VALUES (37, 'IRS - INSTANT REPLAY');
INSERT INTO tags (id, name)
VALUES (38, 'ATT - ATTITUDE');
INSERT INTO tags (id, name)
VALUES (39, 'FIT - FITNESS');
INSERT INTO tags (id, name)
VALUES (40, 'IOT - INDIVIDUAL OFFICIATING TECHNIQUE');
INSERT INTO tags (id, name)
VALUES (41, 'TS - TRANSITION');
INSERT INTO tags (id, name)
VALUES (42, 'ROTATION');
INSERT INTO tags (id, name)
VALUES (43, 'REPORTING');
INSERT INTO tags (id, name)
VALUES (44, 'HCC - HEAD COACH''S CHALLENGE');
INSERT INTO tags (id, name)
VALUES (45, 'LTWD - Long time with defender');
INSERT INTO tags (id, name)
VALUES (46, 'BL/CH - Block Charge');
INSERT INTO tags (id, name)
VALUES (47, 'T&D - Time & Distance');
INSERT INTO tags (id, name)
VALUES (48, 'OFB - Off the Ball');
INSERT INTO tags (id, name)
VALUES (49, 'UF - Unsportsmanlike');
INSERT INTO tags (id, name)
VALUES (50, 'FK - Fake');
INSERT INTO tags (id, name)
VALUES (51, 'PCE - Primary coverage');
INSERT INTO tags (id, name)
VALUES (52, 'EOG - End Of the Game');
INSERT INTO tags (id, name)
VALUES (53, 'EOQ - End Of the Quarter');
INSERT INTO tags (id, name)
VALUES (54, 'EPL - Edge of the Play');
INSERT INTO tags (id, name)
VALUES (55, 'WA - Working area');
INSERT INTO tags (id, name)
VALUES (56, 'PRO - Preventative Officiating');
INSERT INTO tags (id, name)
VALUES (57, 'Check IN');
INSERT INTO tags (id, name)
VALUES (58, 'Check OUT');
INSERT INTO tags (id, name)
VALUES (59, 'OOB - Out-of-Bounds');
INSERT INTO tags (id, name)
VALUES (60, 'SCE - Secondary Coverage');
INSERT INTO tags (id, name)
VALUES (61, 'FAOS - Fouled in Act of Shooting (AOS)');
INSERT INTO tags (id, name)
VALUES (62, 'FNAOS - Foul not in Act of shooting');
INSERT INTO tags (id, name)
VALUES (63, 'AMI - Active mindset');
INSERT INTO tags (id, name)
VALUES (64, 'AOR - Area of responsibility');
INSERT INTO tags (id, name)
VALUES (65, 'SCC - Shot Clock control');
INSERT INTO tags (id, name)
VALUES (66, 'FAC - Fantasy call');

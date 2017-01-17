create  index FK_ART_IDFEED_C on T_ARTICLE (
ART_IDFEED_C
);
create  index FK_AUT_IDUSER_C on T_AUTHENTICATION_TOKEN (
AUT_IDUSER_C
);
create  index FK_USA_IDARTICLE_C on T_USER_ARTICLE (
USA_IDARTICLE_C
);
create  index FK_USA_IDUSER_C on T_USER_ARTICLE (
USA_IDUSER_C
);
create  index FK_USE_IDROLE_C on T_USER (
USE_IDROLE_C
);
create  index FK_USE_IDLOCALE_C on T_USER (
USE_IDLOCALE_C
);
create  index FK_RBF_IDBASEFUNCTION_C on T_ROLE_BASE_FUNCTION (
RBF_IDBASEFUNCTION_C
);
create  index FK_RBF_IDROLE_C on T_ROLE_BASE_FUNCTION (
RBF_IDROLE_C
);
create  index FK_JOE_IDJOB_C on T_JOB_EVENT (
JOE_IDJOB_C
);
create  index FK_JOB_IDUSER_C on T_JOB (
JOB_IDUSER_C
);
create  index FK_FES_IDCATEGORY_C on T_FEED_SUBSCRIPTION (
FES_IDCATEGORY_C
);
create  index FK_FES_IDFEED_C on T_FEED_SUBSCRIPTION (
FES_IDFEED_C
);
create  index FK_FES_IDUSER_C on T_FEED_SUBSCRIPTION (
FES_IDUSER_C
);
create  index FK_CAT_IDPARENT_C on T_CATEGORY (
CAT_IDPARENT_C
);
create  index FK_CAT_IDUSER_C on T_CATEGORY (
CAT_IDUSER_C
);

update t_category set cat_iduser_c='11ea686a-c430-4ffe-8096-4da2e57dc308' where cat_iduser_c='admin';
update t_category set CAT_ID_C='1af6a58d-f0ea-1be7-b695-d3cf4b2e9196' where CAT_ID_C='admin-root';
update t_authentication_token set aut_iduser_c='11ea686a-c430-4ffe-8096-4da2e57dc308' where aut_iduser_c='admin';
update t_user set use_id_c='11ea686a-c430-4ffe-8096-4da2e57dc308' where use_id_c='admin';

alter table T_ARTICLE drop constraint FK_ART_IDFEED_C;
alter table T_AUTHENTICATION_TOKEN drop constraint FK_AUT_IDUSER_C;
alter table T_CATEGORY drop constraint FK_CAT_IDPARENT_C;
alter table T_CATEGORY drop constraint FK_CAT_IDUSER_C;
alter table T_FEED_SUBSCRIPTION drop constraint FK_FES_IDCATEGORY_C;
alter table T_FEED_SUBSCRIPTION drop constraint FK_FES_IDFEED_C;
alter table T_FEED_SUBSCRIPTION drop constraint FK_FES_IDUSER_C;
alter table T_JOB drop constraint FK_JOB_IDUSER_C;
alter table T_JOB_EVENT drop constraint FK_JOE_IDJOB_C;
alter table T_ROLE_BASE_FUNCTION drop constraint FK_RBF_IDBASEFUNCTION_C;
alter table T_ROLE_BASE_FUNCTION drop constraint FK_RBF_IDROLE_C;
alter table T_USER_ARTICLE drop constraint FK_USA_IDARTICLE_C;
alter table T_USER_ARTICLE drop constraint FK_USA_IDUSER_C;
alter table T_USER drop constraint FK_USE_IDLOCALE_C;
alter table T_USER drop constraint FK_USE_IDROLE_C;
alter table T_FEED_SYNCHRONIZATION drop constraint FK_FSY_IDFEED_C;

ALTER TABLE t_article ALTER COLUMN ART_ID_C TYPE UUID USING ART_ID_C::UUID;
ALTER TABLE t_authentication_token alter column AUT_ID_C TYPE UUID USING AUT_ID_C::UUID;
ALTER TABLE t_category alter column CAT_ID_C TYPE UUID USING CAT_ID_C::UUID;
ALTER TABLE t_feed alter column FED_ID_C TYPE UUID USING FED_ID_C::UUID;
ALTER TABLE t_feed_subscription alter column FES_ID_C TYPE UUID USING FES_ID_C::UUID;
ALTER TABLE t_job alter column JOB_ID_C TYPE UUID USING JOB_ID_C::UUID;
ALTER TABLE t_job_event alter column JOE_ID_C TYPE UUID USING JOE_ID_C::UUID;
ALTER TABLE t_user_article alter column USA_ID_C TYPE UUID USING USA_ID_C::UUID;
ALTER TABLE t_user alter column USE_ID_C TYPE UUID USING USE_ID_C::UUID;
ALTER TABLE t_article alter column ART_IDFEED_C TYPE UUID USING ART_IDFEED_C::UUID;
ALTER TABLE t_authentication_token alter column AUT_IDUSER_C TYPE UUID USING AUT_IDUSER_C::UUID;
ALTER TABLE t_category alter column CAT_IDPARENT_C TYPE UUID USING CAT_IDPARENT_C::UUID;
ALTER TABLE t_category alter column CAT_IDUSER_C TYPE UUID USING CAT_IDUSER_C::UUID;
ALTER TABLE t_feed_subscription alter column FES_IDCATEGORY_C TYPE UUID USING FES_IDCATEGORY_C::UUID;
ALTER TABLE t_feed_subscription alter column FES_IDFEED_C TYPE UUID USING FES_IDFEED_C::UUID;
ALTER TABLE t_feed_subscription alter column FES_IDUSER_C TYPE UUID USING FES_IDUSER_C::UUID;
ALTER TABLE t_job alter column JOB_IDUSER_C TYPE UUID USING JOB_IDUSER_C::UUID;
ALTER TABLE t_job_event alter column JOE_IDJOB_C TYPE UUID USING JOE_IDJOB_C::UUID;
ALTER TABLE t_user_article alter column USA_IDARTICLE_C TYPE UUID USING USA_IDARTICLE_C::UUID;
ALTER TABLE t_user_article alter column USA_IDUSER_C TYPE UUID USING USA_IDUSER_C::UUID;
ALTER TABLE t_feed_synchronization alter column FSY_IDFEED_C TYPE UUID USING FSY_IDFEED_C::UUID;


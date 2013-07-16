alter table T_FEED add column FED_RSSBASEURI_C varchar(2000);
alter table T_ARTICLE add column ART_BASEURI_C varchar(2000);
update T_CONFIG set CFG_VALUE_C='1' where CFG_ID_C='DB_VERSION';

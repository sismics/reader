alter table T_USER add column USE_NARROWARTICLE_B bit default 0 not null;
update T_CONFIG set CFG_VALUE_C='2' where CFG_ID_C='DB_VERSION';

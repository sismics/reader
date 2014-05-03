alter table T_FEED_SUBSCRIPTION add column FES_UNREADCOUNT_N int default 0 not null;
update T_CONFIG set CFG_VALUE_C='3' where CFG_ID_C='DB_VERSION';

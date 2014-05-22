alter table T_FEED_SUBSCRIPTION add column FES_UNREADCOUNT_N int default 0 not null;
update T_FEED_SUBSCRIPTION set FES_UNREADCOUNT_N=(select count(a.ART_ID_C) from T_USER_ARTICLE ua join T_ARTICLE a on ua.USA_IDARTICLE_C = a.ART_ID_C where a.ART_IDFEED_C = FES_IDFEED_C and a.ART_DELETEDATE_D is null and ua.USA_READDATE_D is null and ua.USA_DELETEDATE_D is null and ua.USA_IDUSER_C = FES_IDUSER_C);
update T_CONFIG set CFG_VALUE_C='3' where CFG_ID_C='DB_VERSION';

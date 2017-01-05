create index IDX_USA_READDATE_D on T_USER_ARTICLE (USA_READDATE_D, USA_ID_C);
create cached table T_FEED_SYNCHRONIZATION ( FSY_ID_C varchar(36) not null, FSY_IDFEED_C varchar(36) not null, FSY_SUCCESS_B bit not null, FSY_MESSAGE_C longvarchar, FSY_DURATION_N int not null, FSY_CREATEDATE_D datetime not null, primary key (FSY_ID_C) );
alter table T_FEED_SYNCHRONIZATION add constraint FK_FSY_IDFEED_C foreign key (FSY_IDFEED_C) references T_FEED (FED_ID_C) on delete restrict on update restrict;
create index IDX_FSY_CREATEDATE_D on T_FEED_SYNCHRONIZATION (FSY_IDFEED_C, FSY_CREATEDATE_D);
update T_CONFIG set CFG_VALUE_C='5' where CFG_ID_C='DB_VERSION';

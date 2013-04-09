update T_CONFIG set CFG_VALUE_C = 'http://www.reader.com' where CFG_ID_C = 'BASE_URL';
update T_CONFIG set CFG_VALUE_C = '25' where CFG_ID_C = 'EMAIL_SEND_SMTP_PORT';
insert into T_CONFIG (CFG_ID_C, CFG_VALUE_C) values ('BASE_DATA_DIR', '/var/sismicsreader');

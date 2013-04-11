update T_CONFIG set CFG_VALUE_C = 'http://localhost:8080/reader-web' where CFG_ID_C = 'BASE_URL';
update T_CONFIG set CFG_VALUE_C = 'false' where CFG_ID_C = 'CAPTCHA_VALIDATION_ACTIVE';
update T_CONFIG set CFG_VALUE_C = 'RAM' where CFG_ID_C = 'LUCENE_DIRECTOY_STORAGE';
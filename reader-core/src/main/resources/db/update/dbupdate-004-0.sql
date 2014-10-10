update T_USER set USE_THEME_C = 'default' where USE_THEME_C = 'default.less';
update T_USER set USE_THEME_C = 'highcontrast' where USE_THEME_C = 'highcontrast.less';
update T_CONFIG set CFG_VALUE_C='4' where CFG_ID_C='DB_VERSION';

update T_CONFIG set CFG_VALUE_C = 'RAM' where CFG_ID_C = 'LUCENE_DIRECTORY_STORAGE';

-- Test users: Password=12345678
insert into T_USER(USE_ID_C, USE_IDLOCALE_C, USE_IDROLE_C, USE_USERNAME_C, USE_PASSWORD_C, USE_EMAIL_C, USE_THEME_C, USE_DISPLAYTITLEWEB_B, USE_DISPLAYTITLEMOBILE_B, USE_DISPLAYUNREADWEB_B, USE_DISPLAYUNREADMOBILE_B, USE_FIRSTCONNECTION_B, USE_CREATEDATE_D) values('user', 'fr_FR', 'user', 'user', '$2a$10$sz0CcBs45prABgQbN5oK1uQJyGDV2i/AK8RJC8QEVfjxVxtCj7kGW', 'user@localhost', 'default.less', false, true, true, true, false, NOW());
insert into T_USER(USE_ID_C, USE_IDLOCALE_C, USE_IDROLE_C, USE_USERNAME_C, USE_PASSWORD_C, USE_EMAIL_C, USE_THEME_C, USE_DISPLAYTITLEWEB_B, USE_DISPLAYTITLEMOBILE_B, USE_DISPLAYUNREADWEB_B, USE_DISPLAYUNREADMOBILE_B, USE_FIRSTCONNECTION_B, USE_CREATEDATE_D) values('user1', 'en', 'user', 'user1', '$2a$10$sz0CcBs45prABgQbN5oK1uQJyGDV2i/AK8RJC8QEVfjxVxtCj7kGW', 'user1@localhost', 'default.less', false, true, true, true, false, NOW());
insert into T_USER(USE_ID_C, USE_IDLOCALE_C, USE_IDROLE_C, USE_USERNAME_C, USE_PASSWORD_C, USE_EMAIL_C, USE_THEME_C, USE_DISPLAYTITLEWEB_B, USE_DISPLAYTITLEMOBILE_B, USE_DISPLAYUNREADWEB_B, USE_DISPLAYUNREADMOBILE_B, USE_FIRSTCONNECTION_B, USE_CREATEDATE_D) values('user2', 'fr_FR', 'user', 'user2', '$2a$10$sz0CcBs45prABgQbN5oK1uQJyGDV2i/AK8RJC8QEVfjxVxtCj7kGW', 'user2@localhost', 'default.less', false, true, true, true, false, NOW());

insert into T_FEED(FED_ID_C, FED_RSSURL_C, FED_URL_C, FED_TITLE_C, FED_DESCRIPTION_C, FED_CREATEDATE_D) values('space', 'https://www.space.com/home/feed/site.xml', 'https://www.space.com/', 'SPACE.com', 'Something amazing every day.', NOW());
insert into T_FEED(FED_ID_C, FED_RSSURL_C, FED_URL_C, FED_TITLE_C, FED_DESCRIPTION_C, FED_CREATEDATE_D) values('slashdot', 'http://rss.slashdot.org/Slashdot/slashdot', 'https://slashdot.org/', 'Slashdot', 'News for nerds, stuff that matters', NOW());
insert into T_FEED(FED_ID_C, FED_RSSURL_C, FED_URL_C, FED_TITLE_C, FED_DESCRIPTION_C, FED_CREATEDATE_D) values('techcrunch', 'https://feeds.feedburner.com/TechCrunch', 'https://techcrunch.com', 'TechCrunch', 'TechCrunch is a group-edited blog that profiles the companies, products and events defining and transforming the new web.', NOW());
insert into T_FEED(FED_ID_C, FED_RSSURL_C, FED_URL_C, FED_TITLE_C, FED_DESCRIPTION_C, FED_CREATEDATE_D) values('xkcd', 'https://xkcd.com/rss.xml', 'https://xkcd.com/', 'xkcd.com', 'xkcd.com: A webcomic of romance and math humor.', NOW());

insert into T_CATEGORY(CAT_ID_C, CAT_IDUSER_C, CAT_IDPARENT_C, CAT_NAME_C, CAT_ORDER_N, CAT_FOLDED_B, CAT_CREATEDATE_D) values('user-root', 'user', null, null, 0, false, NOW());
insert into T_CATEGORY(CAT_ID_C, CAT_IDUSER_C, CAT_IDPARENT_C, CAT_NAME_C, CAT_ORDER_N, CAT_FOLDED_B, CAT_CREATEDATE_D) values('user1-root', 'user1', null, null, 0, false, NOW());
insert into T_CATEGORY(CAT_ID_C, CAT_IDUSER_C, CAT_IDPARENT_C, CAT_NAME_C, CAT_ORDER_N, CAT_FOLDED_B, CAT_CREATEDATE_D) values('user2-root', 'user2', null, null, 0, false, NOW());
insert into T_CATEGORY(CAT_ID_C, CAT_IDUSER_C, CAT_IDPARENT_C, CAT_NAME_C, CAT_ORDER_N, CAT_FOLDED_B, CAT_CREATEDATE_D) values('user1-technology', 'user1', 'user1-root', 'Technology', 0, false, NOW());
insert into T_CATEGORY(CAT_ID_C, CAT_IDUSER_C, CAT_IDPARENT_C, CAT_NAME_C, CAT_ORDER_N, CAT_FOLDED_B, CAT_CREATEDATE_D) values('user1-comics', 'user1', 'user1-root', 'Comics', 1, false, NOW());

insert into T_FEED_SUBSCRIPTION(FES_ID_C, FES_IDUSER_C, FES_IDFEED_C, FES_IDCATEGORY_C, FES_ORDER_N, FES_CREATEDATE_D) values('user1-space', 'user1', 'space', 'user1-root', 0, NOW());
insert into T_FEED_SUBSCRIPTION(FES_ID_C, FES_IDUSER_C, FES_IDFEED_C, FES_IDCATEGORY_C, FES_ORDER_N, FES_CREATEDATE_D) values('user1-slashdot', 'user1', 'slashdot', 'user1-technology', 0, NOW());
insert into T_FEED_SUBSCRIPTION(FES_ID_C, FES_IDUSER_C, FES_IDFEED_C, FES_IDCATEGORY_C, FES_ORDER_N, FES_CREATEDATE_D) values('user1-techcrunch', 'user1', 'techcrunch', 'user1-technology', 1, NOW());
insert into T_FEED_SUBSCRIPTION(FES_ID_C, FES_IDUSER_C, FES_IDFEED_C, FES_IDCATEGORY_C, FES_ORDER_N, FES_CREATEDATE_D) values('user1-xkcd', 'user1', 'xkcd', 'user1-comics', 0, NOW());

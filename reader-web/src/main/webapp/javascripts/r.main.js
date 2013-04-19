/**
 * Application modules.
 */
var r = {
  main: {
    mobile: false // True if mobile context
  },
  user: {},
  subscription: {},
  category: {},
  feed: {},
  article: {},
  search: {},
  settings: {},
  about: {},
  wizard: {},
  theme: {},
  util: {}
};

/**
 * Application entry point.
 */
$(document).ready(function() {
  r.main.mobile = $('#subscriptions-show-button').is(':visible');
  
  // Displaying login if necessary
  r.util.init();
  r.user.init();
  r.user.boot();
});

/**
 * Modules initialization.
 */
r.main.initModules = function() {
  // Load i18n synchronously
  i18n.init({
    fallbackLng: 'en',
    lng: r.user.userInfo.locale,
    useCookie: false,
    getAsync: false,
    resGetPath: 'locales/messages.__lng__.js',
    debug: false
  });
  $('html').i18n();
  
  // Initializing moment.js i18n
  var language = r.user.userInfo.locale.split('_')[0];
  if (moment.langData(language)) {
    moment.lang(language);
  } else {
    moment.lang('en');
  }
  
  // Load modules together
  r.subscription.init();
  r.feed.init();
  r.category.init();
  r.article.init();
  r.search.init();
  r.settings.init();
  r.about.init();
  r.wizard.init();
  r.theme.init();
  
  // First page
  if (r.user.userInfo.is_admin && r.user.userInfo.first_connection) {
    window.location.hash = '#/wizard/';
  } else if (window.location.hash.length == 0) {
    window.location.hash = '#/feed/unread';
  }
};

/**
 * Reset current page context to show a new view.
 */
r.main.reset = function() {
  $('#toolbar > *').addClass('hidden');
  
  r.feed.reset();
  r.settings.reset();
  r.about.reset();
  r.wizard.reset();
};
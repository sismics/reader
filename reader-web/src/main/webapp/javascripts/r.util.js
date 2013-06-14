/**
 * API URLs.
 */
r.util.url = {
  user_info: 'api/user',
  user_login: 'api/user/login',
  user_update: 'api/user',
  user_logout: 'api/user/logout',
  user_list: 'api/user/list',
  user_register: 'api/user',
  user_username_info: 'api/user/{username}',
  user_username_update: 'api/user/{username}',
  user_username_delete: 'api/user/{username}',
  subscription_list: 'api/subscription',
  subscription_add: 'api/subscription',
  subscription_update: 'api/subscription/{id}',
  subscription_delete: 'api/subscription/{id}',
  subscription_import: 'api/subscription/import',
  subscription_export: 'api/subscription/export',
  subscription_favicon: 'api/subscription/{id}/favicon',
  category_update: 'api/category/{id}',
  category_delete: 'api/category/{id}',
  category_add: 'api/category',
  category_list: 'api/category',
  all: 'api/all',
  starred: 'api/starred',
  starred_star: 'api/starred/{id}',
  article_read: 'api/article/{id}/read',
  article_unread: 'api/article/{id}/unread',
  search: 'api/search/{query}',
  locale_list: 'api/locale',
  theme_list: 'api/theme',
  app_batch_reindex: 'api/app/batch/reindex',
  app_log: 'api/app/log',
  app_version: 'api/app',
  app_map_port: 'api/app/map_port',
  github_tags: 'https://api.github.com/repos/sismics/reader/tags'
};

/**
 * Initialize utility module.
 */
r.util.init = function() {
  // Initialize toastmessage
  $().toastmessage({
    sticky: false,
    position : 'top-center'
  });
  
  // Initialize show/replace pattern
  $('body').on('click', '.show-pattern-button', function() {
    var show = $(this).attr('data-show');
    $(show, this).show();
    $(show + ' input[type="text"]:first', this).focus();
    $(this).hide();
  });
};

/**
 * Wrapper around $.ajax() and basic queue system.
 */
r.util.ajaxQueue = [];
r.util.ajaxQueued = function(queue) {
  return r.util.ajaxQueue.indexOf(queue) != -1;
};
r.util.ajax = function(args, queue) {
  args.dataType = 'json';
  args.cache = false;
  if (!args.fail) {
    args.fail = function(jqxhr) {
      if (jqxhr.responseText) {
        console.log(jqxhr.responseText);
      }
      $().toastmessage('showErrorToast', $.t('error.unknown'));
    }
  }
  
  // Add to queue
  if (queue) {
    r.util.ajaxQueue.push(queue);
  }
  
  $.ajax(args)
    .done(function(data, textStatus, jqXHR) {
      if (queue) {
        r.util.ajaxQueue.splice(r.util.ajaxQueue.indexOf(queue), 1);
      }
      if (args.done) {
        args.done(data, textStatus, jqXHR);
      }
    })
    .fail(function(jqXHR, textStatus, errorThrown) {
      if (queue) {
        r.util.ajaxQueue.splice(r.util.ajaxQueue.indexOf(queue), 1);
      }
      if (args.fail) {
        args.fail(jqXHR, textStatus, errorThrown);
      }
    })
    .always(args.always);
};

/**
 * Returns animated CSS3 loader.
 */
r.util.buildLoader = function() {
  return '<div class="loader"><div id="bowlG"><div id="bowl_ringG"><div class="ball_holderG"><div class="ballG"></div></div></div></div></div>';
};

/**
 * Escape HTML.
 */
r.util.escape = function(str) {
  return $('<div />').text(str).html();
};

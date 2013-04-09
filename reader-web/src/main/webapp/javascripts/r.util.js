/**
 * API URLs.
 */
r.util.url = {
  user_info: 'api/user/info',
  user_login: 'api/user/login',
  user_update: 'api/user/update',
  user_logout: 'api/user/logout',
  user_list: 'api/user/list',
  user_register: 'api/user/register',
  user_username_info: 'api/user/{username}/info',
  user_username_update: 'api/user/{username}/update',
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
  locale_list: 'api/locale/list'
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
 * Wrapper around $.ajax().
 */
r.util.ajax = function(args) {
  args.dataType = 'json';
  args.cache = false;
  if (!args.fail) {
    args.fail = function(jqxhr) {
      if (jqxhr.responseText) {
        console.log(jqxhr.responseText);
      }
      $().toastmessage('showErrorToast', 'Error accessing Reader, try again');
    }
  }
  
  $.ajax(args)
    .done(args.done)
    .fail(args.fail)
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

/**
 * Current user informations.
 */
r.user.userInfo = null;

/**
 * Initializing user module.
 */
r.user.init = function() {
  // Click on login button
  $('#login-submit-button').click(function() {
    var username = $('#login-username-input').val();
    var password = $('#login-password-input').val();
    var remember = $('#login-remember-input').is(':checked');
    
    r.util.ajax({
      url: r.util.url.user_login,
      type: 'POST',
      data: {username: username, password: password, remember: remember},
      done: function(data) {
        // Retrying to boot application
        r.user.boot();
      },
      fail: function(data) {
        // Login fail
        alert('Bad username or password');
      }
    });
    
    // Prevent form submission
    return false;
  });
};

/**
 * Booting application.
 */
r.user.boot = function() {
  r.util.ajax({
    url: r.util.url.user_info,
    type: 'GET',
    done: function(data) {
      // Default password warning
      if (data.is_default_password) {
        $('#default-password').show();
        $('#default-password-info').show();
      }
      
      if (data.anonymous) {
        // Current user is anonymous, displaying login
        $('#login-page').show();
        $('#login-username-input').focus();
      } else {
        // Hiding login
        $('#login-page').hide();
        
        // Saving user data
        r.user.userInfo = data;
        
        // Modules initialization
        r.main.initModules();
        
        // Triggering hash change
        $.History.trigger();
      }
    }
  });
};

/**
 * User logout.
 */
r.user.logout = function() {
  r.util.ajax({
    url: r.util.url.user_logout,
    type: 'POST',
    done: function(data) {
      window.location.reload();
    }
  });
};

/**
 * Return true if the subscription tree is in unread state.
 */
r.user.isDisplayUnread = function() {
  return r.main.mobile ?
    r.user.userInfo.display_unread_mobile : r.user.userInfo.display_unread_web;
};

/**
 * Update subscription tree unread state preference.
 */
r.user.setDisplayUnread = function(unread) {
  // Mobile or web
  var data = {};
  if (r.main.mobile) {
    r.user.userInfo.display_unread_mobile = unread;
    data.display_unread_mobile = unread;
  } else {
    r.user.userInfo.display_unread_web = unread;
    data.display_unread_web = unread;
  }
  
  // Calling API
  r.util.ajax({
    url: r.util.url.user_update,
    data: data,
    type: 'POST'
  });
};

/**
 * Return true if the feed displays only article titles.
 */
r.user.isDisplayTitle = function() {
  return r.main.mobile ?
    r.user.userInfo.display_title_mobile : r.user.userInfo.display_title_web;
};

/**
 * Update feed display state.
 */
r.user.setDisplayTitle = function(title) {
  // Mobile or web
  var data = {};
  if (r.main.mobile) {
    r.user.userInfo.display_title_mobile = title;
    data.display_title_mobile = title;
  } else {
    r.user.userInfo.display_title_web = title;
    data.display_title_web = title;
  }
  
  // Calling API
  r.util.ajax({
    url: r.util.url.user_update,
    data: data,
    type: 'POST'
  });
};

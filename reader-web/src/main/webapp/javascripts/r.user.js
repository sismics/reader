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
        alert($.t('login.error'));
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
      
      // Load i18n synchronously
      r.user.initI18n(data.locale);
      
      // Hide loader layer
      $('#loader-page').hide();
      
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
        
        // Poll jobs if necessary
        r.user.pollJobs(data);
      }
    }
  });
};

/**
 * Poll server to display jobs in progress.
 * Optional userInfo can be provided to replace the first poll.
 */
r.user.pollJobs = function(userInfo) {
  // Show jobs if necessary and returns true if there is jobs in progress
  var showJobs = function(userInfo) {
    var inProgress = $(userInfo.jobs).length > 0;
    
    // Display progress bars for each job
    if (inProgress) {
      // Jobs title
      var done = true;
      $(userInfo.jobs).each(function (i, job) {
        if (job.feed_success + job.feed_failure != job.feed_total || job.starred_success + job.starred_failure != job.starred_total) {
          done = false;
        }
      });
      var html = '<p>' + (done ? $.t('jobs.done') : $.t('jobs.inprogress')) + (done ? ' <a href="#" class="dismiss">' + $.t('jobs.dismiss') + '</a>' : '') + '</p>';
      
      // Jobs details
      $(userInfo.jobs).each(function (i, job) {
        // Feeds progress bar
        var title = '&lt;b&gt;' + $.t('jobs.feeds') + '&lt;/b&gt; ' + (job.feed_success + job.feed_failure) + '/' + job.feed_total;
        if (job.feed_failure > 0) {
          title += ' (' + job.feed_failure + ' ' + $.t('jobs.failed') + ')';
        }
        var progress = Math.round((job.feed_success + job.feed_failure) / job.feed_total * 100);
        html += '<div class="bar"><div class="label">' + $.t('jobs.feeds') + '&nbsp;</div>'
          + '<div class="job" title="' + title + '" ><div class="progress" style="width: ' + progress + '%;"></div></div></div>';
        
        // Starred articles progress bar
        var title = '&lt;b&gt;' + $.t('jobs.starred') + '&lt;/b&gt; ' + (job.starred_success + job.starred_failure) + '/' + job.starred_total;
        if (job.starred_failure > 0) {
          title += ' (' + job.starred_failure + ' ' + $.t('jobs.failed') + ')';
        }
        var progress = Math.round((job.starred_success + job.starred_failure) / job.starred_total * 100);
        html += '<div class="bar"><div class="label">' + $.t('jobs.starred') + '&nbsp;</div>'
          + '<div class="job" title="' + title + '" ><div class="progress" style="width: ' + progress + '%;"></div></div></div>';
      });
      
      // Replace HTML in DOM, show jobs and configure tips
      $('#subscriptions .jobs')
        .html(html)
        .show()
        .find('.job')
        .qtip({
          content: { attr: 'title' },
          position: {
            my: 'top center',
            at: 'bottom center',
            effect: false,
            viewport: $(window),
            adjust: { method: 'shift' }
          },
          style: { classes: 'qtip-light qtip-shadow qtip-job' }
        });
      
      // Dismiss jobs
      $('#subscriptions .jobs .dismiss').click(function() {
        $(userInfo.jobs).each(function (i, job) {
          r.util.ajax({
            url: r.util.url.job_delete.replace('{id}', job.id),
            type: 'DELETE'
          });
        });
        
        $('#subscriptions .jobs').hide();
        return false;
      });
    } else {
      // No job in progress
      $('#subscriptions .jobs').hide();
    }
    
    return inProgress;
  };
  
  if (userInfo && !showJobs(userInfo)) {
    return;
  }
  
  // Poll server
  var intervalId = setInterval(function() {
    r.util.ajax({
      url: r.util.url.user_info,
      type: 'GET',
      done: function(data) {
        if (!showJobs(data)) {
          // Remove polling if there is no more job in progress
          clearInterval(intervalId);
        }
      }
    });
  }, 5000);
};

/**
 * Load i18n strings with 2-letters ISO language _ 2-letters ISO country code.
 */
r.user.initI18n = function(language) {
  language = language.toLowerCase().replace('_', '-');
  isoLanguage = language.split('-')[0];
  
  // Load i18next synchronously
  i18n.init({
    fallbackLng: 'en',
    lng: language,
    useCookie: false,
    getAsync: false,
    resGetPath: 'locales/messages.__lng__.js',
    debug: false
  });
  $('html').i18n();
  
  // Initializing moment.js i18n
  if (moment.langData(language)) {
    moment.lang(language);
  } else {
    moment.lang('en');
  }
  
  // Initializing numeral.js i18n
  try {
    numeral.language(language);
  } catch (e) {
    numeral.language('en');
  }
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
 * Returns true if the connected user has a base function.
 */
r.user.hasBaseFunction = function(baseFunction) {
  if (r.user.userInfo == null) {
    return false;
  }
  
  var found = false;
  $(r.user.userInfo.base_functions).each(function(i, base) {
    if (base == baseFunction) {
      found = true;
    }
  });
  
  return found;
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

/**
 * Return true if the feed displays narrow articles.
 */
r.user.isNarrowArticle = function() {
  return r.user.userInfo.narrow_article;
};

/**
 * Update feed display narrow articles.
 */
r.user.setNarrowArticle = function(narrow) {
  r.user.userInfo.narrow_article = narrow;

  // Calling API
  r.util.ajax({
    url: r.util.url.user_update,
    data: {
      narrow_article: narrow
    },
    type: 'POST'
  });
};
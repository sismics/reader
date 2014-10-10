/**
 * Reset settings related context.
 */
r.settings.reset = function() {
  // Hiding settings container
  $('#settings-container').hide();
};

/**
 * Initializing settings module.
 */
r.settings.init = function() {
  // Listening hash changes on #/settings/*
  // /settings/
  $.History.bind('/settings/', function(state, target) {
    // Resetting page context
    r.main.reset();
    
    // Showing settings container
    $('#settings-container').show();
    
    // Configuring contextual toolbar
    $('#toolbar > .settings').removeClass('hidden');
    
    // Removing admin tab if necessary
    if (!r.user.hasBaseFunction('ADMIN')) {
      $('#settings-tab-users').remove();
      $('#settings-tabs a[href="#settings-tab-users"]').parent().remove();
    }
    
    // Initialize tabs
    $('#settings-tabs').tabs({
      create: function(e, ui) {
        r.settings.onTabActivated(ui.panel);
      },
      activate: function(e, ui) {
        r.settings.onTabActivated(ui.newPanel);
      }
    });
  });
  
  // Toolbar: settings qtip
  $('#toolbar > .settings-button').qtip({
    content: { text: $('#qtip-settings') },
    position: {
      my: 'top right',
      at: 'bottom center',
      effect: false,
      viewport: $(window),
      adjust: { method: 'shift' }
    },
    show: { event: 'click' },
    hide: { event: 'click unfocus' },
    style: { classes: 'qtip-light qtip-shadow' }
  });
  
  // Configuring settings qtip
  $('#qtip-settings').click(function() {
    $('#toolbar > .settings-button').qtip('hide');
  });
  
  $('#qtip-settings .logout').click(function(e) {
    r.user.logout();
    e.stopPropagation();
  });
  
  // Submitting import form
  $('#settings-import-form input[type="submit"]').click(function() {
    var _this = $(this);
    
    // Adding file data to form data
    var formData = new FormData();
    var file = $('#settings-import-form input[type="file"]')[0].files[0];
    formData.append('file', file);
    
    // Disable button during request to avoid double upload
    _this.attr('disabled', 'disabled');
    $('#settings-import-form .ajax-loader').removeClass('hidden');
    
    // Calling API
    r.util.ajax({
      url: r.util.url.subscription_import,
      type: 'PUT',
      data: formData,
      processData: false,
      contentType: false,
      done: function(data) {
        alert($.t('settings.import.success'));
        
        // Start feedback on job progression
        r.user.pollJobs();
      },
      fail: function(data) {
        // Login fail
        alert($.t('settings.import.error'));
      },
      always: function() {
        // Enabling button
        _this.removeAttr('disabled');
        $('#settings-import-form input[type="file"]').val('');
        $('#settings-import-form .ajax-loader').addClass('hidden');
      }
    });
    
    // Prevent form submission
    return false;
  });
  
  // Click on export button
  $('#settings-export-button').click(function() {
    // Redirecting to API
    window.location.href =  r.util.url.subscription_export;
  });
};

/**
 * Triggers when a tab is visible.
 */
r.settings.onTabActivated = function(panel) {
  var initialized = panel.data('initialized');
  panel.data('initialized', true);
  
  switch (panel.attr('id')) {
  case 'settings-tab-account':
    r.settings.onTabAccount(panel, !initialized);
    break;
  case 'settings-tab-import':
    r.settings.onTabImport(panel, !initialized);
    break;
  case 'settings-tab-users':
    r.settings.onTabUsers(panel, !initialized);
    break;
  }
};

/**
 * Triggers when import tab is activated.
 */
r.settings.onTabImport = function(panel, initialize) {
  if (initialize) {
    // Hide import base function if current user don't have
    if(!r.user.hasBaseFunction('IMPORT')) {
      $('#settings-tab-import .import-base-function').hide();
    }
  }
}

/**
 * Triggers when account tab is activated.
 */
r.settings.onTabAccount = function(panel, initialize) {
  // Account edit form
  var form = panel.find('#settings-account-edit-form');
  var emailInput = form.find('.edit-email-input');
  var localeInput = form.find('.edit-locale-input');
  var themeInput = form.find('.edit-theme-input');
  var passwordInput = form.find('.edit-password-input');
  var password2Input = form.find('.edit-password2-input');
  
  // Initialize values
  emailInput.val(r.user.userInfo.email);
  localeInput.val(r.user.userInfo.locale);
  themeInput.val(r.user.userInfo.theme);
  passwordInput.val('');
  password2Input.val('');
  
  if (initialize) {
    // Cancel button
    form.find('.edit-cancel-button').click(function() {
      window.location.hash = '#/feed/unread';
    });
    
    // Change theme on select change
    themeInput.change(function() {
      r.theme.update($(this).val());
    });
    
    // Populating themes from server
    var userTheme = r.user.userInfo.theme;
    r.util.ajax({
      url: r.util.url.theme_list,
      type: 'GET',
      done: function(data) {
        var html = '';
        $(data.themes).each(function(i, theme) {
          html += '<option value="' + theme.id + '"' + (userTheme == theme.id ? ' selected' : '') + '>' +  $.t('theme.' + theme.id + '.name') + '</option>';
        });
        form.find('.edit-theme-input').html(html);
      }
    });
    
    // Populating locales from server
    var userLocale = r.user.userInfo.locale;
    r.util.ajax({
      url: r.util.url.locale_list,
      type: 'GET',
      done: function(data) {
        var html = '';
        $(data.locales).each(function(i, locale) {
          html += '<option value="' + locale.id + '"' + (userLocale == locale.id ? ' selected' : '') + '>' + $.t('locale.' + locale.id) + '</option>';
        });
        form.find('.edit-locale-input').html(html);
      }
    });
    
    // Hide password base function if current user don't have
    if(!r.user.hasBaseFunction('PASSWORD')) {
      $('#settings-account-edit-form .password-base-function').hide();
    }
    
    // Account edit form validation
    form.validate({
      rules: {
        email: {
          required: true,
          email: true,
          minlength: 3,
          maxlength: 50
        },
        password: {
          minlength: 8,
          maxlength: 50
        },
        password2: { equalTo: '#' + form.attr('id') + ' .edit-password-input' }
      },
      submitHandler: function() {
        // Calling API
        var email = emailInput.val();
        var locale = localeInput.val();
        var theme = themeInput.val();
        var password = passwordInput.val();
        
        r.util.ajax({
          url: r.util.url.user_update,
          type: 'POST',
          data: {
            email: email,
            locale: locale,
            theme: theme,
            password: password
          },
          done: function(data) {
            r.user.userInfo.email = email;
            r.user.userInfo.locale = locale;
            r.user.userInfo.theme = theme;
            $().toastmessage('showSuccessToast', $.t('settings.account.edit.success'));
          }
        });
        
        return false;
      }
    });
  }
};

/**
 * Triggers when users tab is activated.
 */
r.settings.onTabUsers = function(panel, initialize) {
  var form = $('#settings-users-edit-form');
  var selectInput = $('#settings-users-select');
  var usernameInput = form.find('.edit-username-input');
  var emailInput = form.find('.edit-email-input');
  var localeInput = form.find('.edit-locale-input');
  var passwordInput = form.find('.edit-password-input');
  var password2Input = form.find('.edit-password2-input');
  var deleteSection = form.find('.edit-delete-section');
  var deleteButton = form.find('.edit-delete-button');
  
  if (initialize) {
    // User add/edit form validation
    var validate = form.validate({
      rules: {
        username: {
          required: true
        },
        email: {
          required: true,
          email: true,
          minlength: 3,
          maxlength: 50
        },
        password: {
          minlength: 8,
          maxlength: 50
        },
        password2: { equalTo: '#' + form.attr('id') + ' .edit-password-input' }
      },
      submitHandler: function() {
        // Calling API
        var username = usernameInput.val();
        var email = emailInput.val();
        var locale = localeInput.val();
        var password = passwordInput.val();
        var newUser = !selectInput.val();
        
        r.util.ajax({
          url: newUser ?
            r.util.url.user_register :
            r.util.url.user_username_update.replace('{username}', username),
          type: newUser ? 'PUT' : 'POST',
          data: {
            username: username,
            email: email,
            locale: locale,
            password: password
          },
          done: function(data) {
            if (newUser) {
              $().toastmessage('showSuccessToast', $.t('settings.users.edit.successNew'));
              
              // Reset form and add user to select
              form[0].reset();
              selectInput.append($('<option value="' + username + '">' + username + '</option>'));
            } else {
              $().toastmessage('showSuccessToast', $.t('settings.users.edit.successUpdate'));
            }
          }
        });
        
        return false;
      }
    });
    
    // User selected
    selectInput.change(function() {
      var username = $(this).val();
      validate.resetForm();

      if (!username) {
        // New user case
        usernameInput.removeAttr('disabled');
        deleteSection.hide();
        
        // Title
        form.find('h2').html($.t('settings.users.edit.newtitle'));
        
        // Resetting fields
        usernameInput.val('');
        emailInput.val('');
        localeInput.val(r.user.userInfo.locale);
        passwordInput.val('').rules('add', { required: true });
        password2Input.val('');
      } else {
        // Edit user case
        usernameInput.attr('disabled', 'disabled');
        deleteSection.show();
        
        // Title
        form.find('h2').html(username);
        
        // Calling API
        r.util.ajax({
          url: r.util.url.user_username_info.replace('{username}', username),
          type: 'GET',
          done: function(data) {
            usernameInput.val(data.username);
            emailInput.val(data.email);
            localeInput.val(data.locale);
            passwordInput.val('').rules('remove', 'required');
            password2Input.val('');
          }
        });
      }
    });
    
    // User delete
    deleteButton.click(function() {
      if (!confirm($.t('settings.users.edit.deleteconfirm'))) {
        return;
      }
      
      var username = usernameInput.val();
      
      // Calling API
      r.util.ajax({
        url: r.util.url.user_username_delete.replace('{username}', username),
        type: 'DELETE',
        done: function(data) {
          // Remove user from select
          selectInput
            .val('')
            .change()
            .find('option[value="' + username + '"]')
            .remove();
        }
      });
    });
    
    // Fetching users from API
    r.util.ajax({
      url: r.util.url.user_list,
      type: 'GET',
      data: { offset: 0, limit: 100 },
      done: function(data) {
        var html = '<option value="">' + $.t('settings.users.edit.newtitle') + '</option>';
        $(data.users).each(function(i, user) {
          html += '<option value="' + user.username + '">' + user.username + '</option>';
        });
        $('#settings-users-select').html(html);
      }
    });
  }
  
  // Copying locales list
  form.find('.edit-locale-input').html($('#settings-account-edit-form .edit-locale-input').html());
  
  // Force change trigger to update view
  $('#settings-users-select').trigger('change');
};

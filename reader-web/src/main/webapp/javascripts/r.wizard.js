/**
 * Reset wizard related context.
 */
r.wizard.reset = function() {
  // Hiding wizard container
  $('#wizard-container').hide();
};

/**
 * Initializing wizard module.
 */
r.wizard.init = function() {
  // Listening hash changes on #/wizard/*
  // /wizard/
  $.History.bind('/wizard/', function(state, target) {
    // Resetting page context
    r.main.reset();
    
    // Showing wizard container
    $('#wizard-container').show();
    
    // Removing warning
    $('#default-password').hide();
    
    // Configuring contextual toolbar
    $('#toolbar > .wizard').removeClass('hidden');
    
    // Go to first page
    r.wizard.changePage(0);
  });
};

/**
 * Wizard page change.
 */
r.wizard.changePage = function(pageIndex) {
  var containerId = '#wizard-step-' + pageIndex + '-container';
  
  // Hightlight current page
  $('#wizard-container > ul li').removeClass('active');
  $('#wizard-container > ul li[data-wizard-container="' + containerId + '"]').addClass('active');
  
  // Show current page
  $('#wizard-container .wizard-container').hide();
  $(containerId).show();
  
  // Navigation previous
  var previous = $('#wizard-navigation-previous');
  if (pageIndex == 0) {
    previous.hide();
  } else {
    previous.show();
    previous.unbind().click(function() {
      r.wizard.changePage(pageIndex - 1);
    });
  }
  
  // Navigation next
  $('#wizard-navigation-next').unbind().click(function() {
    if (r.wizard.onNextStep[pageIndex]()) {
      r.wizard.changePage(pageIndex + 1);
    }
  });
};

/**
 * Wizard validation listeners.
 */
r.wizard.onNextStep = { 0: false, 1: false, 2: false };

/**
 * Validate wizard page 0.
 */
r.wizard.onNextStep[0] = function () {
  var form = $('#wizard-step-0-form');
  var passwordInput = form.find('.wizard-password-input');
  var password2Input = form.find('.wizard-password2-input');
  
  // Form validation
  if (passwordInput.val() == '') {
    if (confirm($.t('wizard.keepdefaultpassword'))) {
      return true;
    } else {
      return false;
    }
  }
  
  if (passwordInput.val() != password2Input.val()) {
    alert($.t('wizard.passwordconfirmerror'));
    return false;
  }
  
  if (passwordInput.val().length < 8) {
    alert($.t('wizard.passwordtooshort'));
    return false;
  }
  
  // Calling API
  r.util.ajax({
    url: r.util.url.user_update,
    type: 'POST',
    data: {
      password: passwordInput.val(),
      first_connection: false
    }
  });
  
  return true;
};

/**
 * Validate wizard page 1.
 */
r.wizard.onNextStep[1] = function () {
  var form = $('#wizard-step-1-form');
  var upnpInput = form.find('.wizard-upnp-input');
  
  if (upnpInput.is(':checked')) {
    // Calling API
    r.util.ajax({
      url: r.util.url.app_map_port,
      type: 'POST',
      fail: function() {
        alert($.t('wizard.upnperror'));
      }
    });
  }
  
  return true;
};

/**
 * Validate wizard page 2.
 */
r.wizard.onNextStep[2] = function () {
  var form = $('#wizard-step-2-form');
  var usernameInput = form.find('.wizard-username-input');
  var emailInput = form.find('.wizard-email-input');
  var passwordInput = form.find('.wizard-password-input');
  var password2Input = form.find('.wizard-password2-input');
  
  // Form validation
  form.validate({
    rules: {
      username: {
        required: true,
        minlength: 3,
        maxlength: 50
      },
      email: {
        required: true,
        email: true,
        minlength: 3,
        maxlength: 50
      },
      password: {
        required: true,
        minlength: 8,
        maxlength: 50
      },
      password2: { equalTo: '#' + form.attr('id') + ' .wizard-password-input' }
    }
  });
  
  if (form.valid()) {
    // Calling API
    var username = usernameInput.val();
    var email = emailInput.val();
    var password = passwordInput.val();
    
    r.util.ajax({
      url: r.util.url.user_register,
      type: 'PUT',
      data: {
        username: username,
        email: email,
        password: password
      },
      done: function(data) {
        $().toastmessage('showSuccessToast', $.t('wizard.installationcompleted'));
        window.location.hash = '#/feed/unread';
      }
    });
  }
  
  return false;
};

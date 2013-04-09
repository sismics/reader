var ajaxWait = 200;
var r, j = null;

afterEach(function () {
  // check if any have failed
  if(this.results_.failedCount > 0) {
    // if so, change the function which should move to the next test
    jasmine.Queue.prototype.next_ = function () {
      // to instead skip to the end
      this.onComplete();
    }
  }
});

describe('Settings', function() {
  document.cookie="auth_token=;path=/";
  
  it('should be able to init', function() {
    $('body').append('<iframe width="1100" height="800" name="iframe2" src="../index.html"></iframe>');
    waitsFor(function() {
      return window['iframe2'].r != null;
    }, 1000);
    
    runs(function() {
      r = window['iframe2'].r;
      j = window['iframe2'].jQuery;
    });
    
    waits(ajaxWait);
    
    runs(function() {
      expect(r).not.toBe(null);
    });
  });
  
  it('should be able to login', function() {
    runs(function() {
      j('#login-username-input').val('user1');
      j('#login-password-input').val('12345678');
      j('#login-submit-button').click();
    });

    waits(600);
    
    runs(function() {
      expect(j('#feed-container').is(':visible')).toBe(true);
    });
  });
  
  it('should be able to change settings', function() {
    runs(function() {
      j('#toolbar .settings-button').click();
    });
    
    waits(ajaxWait);
    
    runs(function() {
      j('#qtip-settings a')[0].click();
    });
    
    waits(ajaxWait);
    
    runs(function() {
      j('#settings-account-edit-form .edit-email-input').val('test1@reader.com');
      j('#settings-account-edit-form .edit-locale-input').val('fr');
      j('#settings-account-edit-form .edit-password-input').val('123456789');
      j('#settings-account-edit-form .edit-password2-input').val('123456789');
      j('#settings-account-edit-form input[type="submit"]').click();
    });
    
    waits(ajaxWait);
    
    runs(function() {
      j('#settings-account-edit-form .edit-email-input').val('test1@reader.com');
      j('#settings-account-edit-form .edit-locale-input').val('fr');
      j('#settings-account-edit-form .edit-password-input').val('123456789');
      j('#settings-account-edit-form .edit-password2-input').val('123456789');
      j('#settings-account-edit-form input[type="submit"]').click();
    });
  });
  
  it('should be able to change settings again', function() {
    runs(function() {
      j('#unread-feed-button a')[0].click();
    });
    
    waits(ajaxWait);
    
    runs(function() {
      j('#toolbar .settings-button').click();
    });
    
    waits(ajaxWait);
    
    runs(function() {
      j('#qtip-settings a')[0].click();
    });
    
    waits(ajaxWait);
    
    runs(function() {
      expect(j('#settings-account-edit-form .edit-email-input').val()).toEqual('test1@reader.com');
      expect(j('#settings-account-edit-form .edit-locale-input').val()).toEqual('fr');
      j('#settings-account-edit-form .edit-password-input').val('12345678');
      j('#settings-account-edit-form .edit-password2-input').val('12345678');
      j('#settings-account-edit-form input[type="submit"]').click();
    });
    
    waits(ajaxWait);
  });
});
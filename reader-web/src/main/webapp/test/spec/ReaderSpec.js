var ajaxWait = 200;
var r, j, category, categoryId = null;

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

describe('Reader', function() {
  document.cookie="auth_token=;path=/";
  
  it('should be able to init', function() {
    $('body').append('<iframe width="1100" height="800" name="iframe1" src="../index.html"></iframe>');
    waitsFor(function() {
      return window['iframe1'].r != null;
    }, 1000);
    
    runs(function() {
      r = window['iframe1'].r;
      j = window['iframe1'].jQuery;
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
  
  it('should be able to display all feed', function() {
    runs(function() {
      j('#all-feed-button a')[0].click();
    });
    
    waits(ajaxWait);
    
    runs(function() {
      expect(j('#feed-container .feed-item').size()).toBeGreaterThan(0);
    });
  });
  
  it('should be able to add a category', function() {
    runs(function() {
      j('#category-add-button').click();
    });
    
    waits(ajaxWait);
    
    category = 'Category test ' + new Date().getTime();
    runs(function() {
      j('#category-name-input').val(category);
      j('#category-submit-button').click();
    });
    
    waits(ajaxWait);
    
    runs(function() {
      if (r.user.userInfo.display_unread_web) {
        j('#subscription-unread-button').click();
      }
    });
    
    waits(ajaxWait);
    
    runs(function() {
      var link = j('a[title="' + category + '"]');
      var span = j('span:contains("' + category + '")');
      expect(span.size()).toEqual(1);
      link[0].click();
    });
    
    waits(ajaxWait);
    
    runs(function() {
      expect(j('#feed-container .feed-item').size()).toEqual(0);
    });
  });
  
  it('should be able to add a subscription', function() {
    runs(function() {
      j('#subscription-add-button').click();
    });
    
    waits(ajaxWait);
    
    runs(function() {
      j('#subscription-url-input').val('http://www.bgamard.org/reader/feed_rss2_korben.xml');
      j('#subscription-submit-button').click();
    });
    
    waits(3000);
  });
  
  it('should be able to change the category', function() {
    runs(function() {
      j('#toolbar .category-button').click();
    });
    
    waits(ajaxWait);
    
    runs(function() {
      var li = j('.qtip .qtip-change-category li:contains("' + category + '")');
      categoryId = li.attr('data-category-id');
      li.click();
    });
    
    waits(ajaxWait);
    
    runs(function() {
      expect(j('#category-' + categoryId + ' #subscription-' + r.feed.context.subscriptionId).size()).toEqual(1);
    });
  });
  
//  it('should be able to delete the subscription', function() {
//    runs(function() {
//      j('#category-' + categoryId + ' #subscription-' + r.feed.context.subscriptionId + ' .edit').click();
//    });
//    
//    waits(ajaxWait);
//    
//    runs(function() {
//      j('.qtip-subscription-edit:visible')
//    });
//  });
});
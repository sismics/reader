/**
 * Current feed context.
 */
r.feed.context = {
  unread: true, // Unread state
  subscriptionId: null, // Subscription ID if relevant
  categoryId: null, // Category ID if relevant
  url: null, // API URL
  loading: false, // True if XHR in progress
  limit: function() { // Articles number to fetch each page
    return $('#feed-container').hasClass('list') ? 15 : 10;
  }, 
  lastItem: null, // Last article
  bumper: null, // Bumper
  fullyLoaded: false, // True if all articles are loaded
  activeXhr: null // Active XHR retrieving a feed
};

/**
 * Reset feed related context.
 */
r.feed.reset = function() {
  // Hiding articles container
  $('#feed-container').hide();
  
  // Resetting highlights
  $('#subscriptions li.active').removeClass('active');
};

/**
 * Initializing feed module.
 */
r.feed.init = function() {
  // Listening hash changes on #/feed/*
  // /feed/all
  // /feed/unread
  // /feed/starred
  // /feed/subscription/id
  // /feed/category/id
  // /feed/search/query
  $.History.bind('/feed/', function(state, target) {
    // Resetting page context
    r.main.reset();
    
    // Showing articles container
    $('#feed-container').show();
    
    // Configuring contextual toolbar
    $('#toolbar > .feed').removeClass('hidden');
    
    // Resetting context
    r.feed.context.subscriptionId = null;
    r.feed.context.categoryId = null;
    
    // Building feed context
    if (target == 'all') {
      // Configuring context for /all
      r.feed.context.url = r.util.url.all;
      r.feed.context.unread = false;
      $('#all-feed-button').addClass('active');
      
      // Specific toolbar actions for all
      $('#toolbar > .all').removeClass('hidden');
      
    } else if (target == 'unread') {
      // Configuring context for /all?unread=true
      r.feed.context.url = r.util.url.all;
      r.feed.context.unread = true;
      $('#unread-feed-button').addClass('active');
      
      // Specific toolbar actions for unread
      $('#toolbar > .unread').removeClass('hidden');
      
    } else if (target == 'starred') {
      // Configuring context for /starred
      r.feed.context.url = r.util.url.starred;
      r.feed.context.unread = false;
      $('#starred-feed-button').addClass('active');
      
      // Specific toolbar actions for starred
      $('#toolbar > .starred').removeClass('hidden');
      
    } else if (target.substring(0, 13) == 'subscription/') {
      // Configuring context for /subscription/id
      r.feed.context.url = 'api/' + target;
      r.feed.context.subscriptionId = target.substring(13);
      
      // Specific toolbar actions for subscriptions
      $('#toolbar > .subscription').removeClass('hidden');
      
    } else if (target.substring(0, 9) == 'category/') {
      // Configuring context for /category/id
      r.feed.context.url = 'api/' + target;
      r.feed.context.categoryId = target.substring(9);
      
      // Specific toolbar actions for categories
      $('#toolbar > .category').removeClass('hidden');
    } else if (target.substring(0, 7) == 'search/') {
      // Configuring context for /search/query
      r.feed.context.unread = false;
      r.feed.context.url = r.util.url.search.replace('{query}', target.substring(7));
      
      // Specific toolbar actions for search
      $('#toolbar > .search').removeClass('hidden');
    }
    
    // Focus on articles list
    $('#feed-container').focus();
    
    // Loading articles
    r.feed.load(false);
    
    // Loading subscriptions tree
    r.subscription.update();
  });
  
  // Smartphone and desktop scroll listener
  $(window).scroll(r.feed.scroll);
  $('#feed-container').scroll(r.feed.scroll);
  
  // Toolbar action: refresh
  $('#toolbar .refresh-button').click(function() {
    r.feed.load(false);
    r.subscription.update();
  });
  
  // Toolbar action: show all/new articles
  $('#toolbar .all-button').click(function() {
    r.feed.context.unread = !r.feed.context.unread;
    r.feed.context.unread ? $(this).html($.t('toolbar.showall')) : $(this).html($.t('toolbar.shownew'));
    r.feed.load(false);
  });
  
  // Toolbar action: mark all as read
  $('#toolbar .all-read-button').click(function() {
    r.util.ajax({
      url: r.feed.context.url + '/read',
      type: 'POST',
      always: function() {
        r.feed.load(false);
        r.subscription.update();
      }
    });
  });
  
  // Toolbar action: list mode
  $('#toolbar .list-button').click(function() {
    r.user.setDisplayTitle(true);
    r.feed.updateMode(true);
  });
  
  // Toolbar action: full mode
  $('#toolbar .full-button').click(function() {
    r.user.setDisplayTitle(false);
    r.feed.updateMode(true);
  });
  
  // Update feed mode
  r.feed.updateMode(false);
};

/**
 * Feed scroll listener.
 */
r.feed.scroll = function() {
  var container = $('#feed-container');
  var scroll = r.main.mobile ? $(window).scrollTop() : container.scrollTop();
  var height = r.main.mobile ? $(window).height() / 2 : container.height() / 2;
  var feedItemList = container.find('.feed-item');
  
  // Selected item
  var selected = null;
  var selectedTopAbs = 0;
  feedItemList
    .removeClass('selected')
    .filter(function() {
      return r.article.top($(this), scroll) < height;
    })
    .each(function() {
      var topAbs = Math.abs($(this).data('top'));
      if (selected == null || selectedTopAbs > topAbs) {
        selected = $(this);
        selectedTopAbs = topAbs;
      }
    });
  
  if (selected != null) {
    selected.addClass('selected');
    
    // Mark as read on scroll
    var itemsToRead = feedItemList.slice(0, selected.index() + 1);
    if (container.hasClass('list')) itemsToRead = itemsToRead.filter('.unfolded');
    itemsToRead = itemsToRead.not('.read, .forceunread');
    itemsToRead.each(function() {
      r.article.read($(this), true);
    });
  }
  
  // Paging
  r.feed.triggerPaging();
  
  // Optimizing
  r.feed.optimize();
};

/**
 * Load articles according to the feed context.
 */
r.feed.load = function(next) {
  if (r.feed.context.loading) {
    if (next) {
      // Stop if already loading articles
      return;
    } else if (r.feed.activeXhr) {
      // Cancel ongoing request
      r.feed.activeXhr.abort();
    }
  }
  
  if (!next) {
    // Loading animation
    $('#feed-container').html(r.util.buildLoader());
    
    // Updating show all/show new button
    r.feed.context.unread ? $('#toolbar .all-button').html($.t('toolbar.showall')) : $('#toolbar .all-button').html($.t('toolbar.shownew'));
    
    // Reset flag telling if all articles are loaded
    r.feed.context.fullyLoaded = false;
    r.feed.context.lastItem = null;
  }
  
  // All articles are loaded, stop
  if (r.feed.context.fullyLoaded) {
    return;
  }
  
  // Adding bumper
  r.feed.context.loading = true;
  if (r.feed.context.bumper != null) {
    r.feed.context.bumper.find('.loader').show();
  }
  
  // Building payload
  data = {
    unread: r.feed.context.unread,
    limit: r.feed.context.limit
  }
  
  if (r.feed.context.lastItem) {
    data.after_article = r.feed.context.lastItem.attr('data-article-id');
  }
  
  // Special case for search (use offset paging)
  if (next && r.feed.context.url.substring(0, 11) == r.util.url.search.substring(0, 11)) {
    data.offset = $('#feed-container .feed-item').length;
  }
  
  // Calling API
  r.feed.activeXhr = r.util.ajax({
    url: r.feed.context.url,
    type: 'GET',
    data: data,
    done: function(data) {
      var noArticles = $(data.articles).length == 0;
      
      // Pre article build
      if (!next) {
        $('#feed-container').html('');
        
        // Empty placeholder
        if (noArticles) {
          $('#feed-container').append(r.feed.buildEmpty());
        }
        
        // Adding bumper
        var bumper = r.feed.buildBumper(data);
        r.feed.context.bumper = bumper;
        $('#feed-container').append(bumper);
      }
      
      // All articles are loaded?
      r.feed.context.fullyLoaded = noArticles;
      
      // Building articles
      $(data.articles).each(function(i, article) {
        // Escape some fields
        article.subscription.title = r.util.escape(article.subscription.title);
        article.creator = r.util.escape(article.creator);
        
        // Build article
        var item = r.article.build(article);
        r.feed.context.bumper.before(item);
        
        // Store last item
        if(i == $(data.articles).length - 1) {
          r.feed.context.lastItem = item;
        }
      });
      
      // Post article build
      if (!next) {
        // Scrolling to top
        r.feed.scrollTop(0);
        
        // Focus article list and redraw
        $('#feed-container')
          .trigger('focus')
          .redraw();
      }
      
      r.feed.context.loading = false;
      r.feed.activeXhr = null;
      r.feed.context.bumper.find('.loader').hide();
      r.feed.context.bumper.find('.retry').hide();
      
      // Trigger paging in the case that all newly added articles are visible
      r.feed.triggerPaging();
    },
    fail: function(jqXHR, textStatus, errorThrown) {
      r.feed.context.loading = false;
      r.feed.activeXhr = null;
      $('#feed-container .loader').hide();
      
      if (textStatus != 'abort') {
        r.feed.context.bumper.find('.retry').show();
        $().toastmessage('showErrorToast', $.t('error.feed'));
      }
    }
  });
};

/**
 * Trigger loading next articles if necessary.
 */
r.feed.triggerPaging = function() {
  // Load next articles when the last article or the bumper is partially visible
  if (r.feed.context.lastItem != null && r.feed.context.lastItem.visible(true) ||
      r.feed.context.bumper != null && r.feed.context.bumper.visible(true)) {
    r.feed.load(true);
  }
};

/**
 * Build empty placeholder according to feed context.
 */
r.feed.buildEmpty = function() {
  var message = $.t('feed.noarticle');
  if (r.feed.context.unread) {
    message = $.t('feed.nonewarticle');
  }
  return $('<div class="empty">' + message + '</div>');
};

/**
 * Build feed bumper according to feed context.
 */
r.feed.buildBumper = function(data) {
  var html = $.t('feed.nomorearticles');
  
  // Build html (note that unread state is irrelevant for searching)
  if (r.feed.context.unread && r.feed.context.url.substring(0, 11) != r.util.url.search.substring(0, 11)) {
    if (data.subscription) {
      html = $.t('feed.subscriptionnomoreunreadarticles', {subscription: data.subscription.title});
    } else {
      html = $.t('feed.nomoreunreadarticles');
    }
    html += '<br /><a href="#" class="showall">' + $.t('feed.showall') + '</a>';
  }
  
  var bumper = $('<div class="bumper"><img class="loader" src="images/ajax-loader.gif" />' +
      '<br /><a href="#" class="retry">' + $.t('feed.retry') + '</a><br />' + html + '</div>');
  
  // Show all link
  bumper.find('.showall').click(function() {
    r.feed.context.unread = false;
    r.feed.load(false);
    return false;
  });
  
  // Retry link
  bumper.find('.retry').click(function() {
    r.feed.triggerPaging();
    $(this).hide();
    return false;
  });
  
  return bumper;
};

/**
 * Optimize articles list by destructing article's DOM outside of the viewport.
 */
r.feed.optimize = $.debounce(350, function() {
  // TODO Smarter articles outside window detection to avoid burning CPU
  
  // It's not necessary to optimize in list mode, the content is not rendered
  if ($('#feed-container').hasClass('list')) {
    return;
  }
  
  // Delete content from articles largely outside
  $('#feed-container .feed-item:largelyoutside')
    .addClass('destroyed')
    .height(function() {
      return $(this).height();
    })
    .html('');
  
  // Rebuild articles near the viewport
  $('#feed-container .feed-item.destroyed:not(:largelyoutside)')
  .removeClass('destroyed')
  .height('auto')
  .replaceWith(function() {
    // Rebuild articles, but keeping state CSS classes
    return r.article.build($(this).data('article'), $(this).attr('class'));
  });
});

/**
 * Scroll to top.
 */
r.feed.scrollTop = function(top, animate) {
  if (animate) {
    $('body, html').animate({ scrollTop: top }, 200);
    $('#feed-container').animate({ scrollTop: top }, 200);
  } else {
    $('body, html').scrollTop(top);
    $('#feed-container').scrollTop(top);
  }
};

/**
 * Update feed mode (list or full) according to user preference.
 */
r.feed.updateMode = function(reload) {
  var list = r.user.isDisplayTitle();
  if (list) {
    $('#feed-container').addClass('list');
  } else {
    $('#feed-container').removeClass('list');
  }
  
  if (reload) {
    // Reload feed
    r.feed.load(false);
  }
};
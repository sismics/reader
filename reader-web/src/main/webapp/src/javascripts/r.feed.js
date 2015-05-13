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
    return r.feed.cache.container.hasClass('list') ? 15 : 10;
  }, 
  lastItem: null, // Last article
  bumper: null, // Bumper
  fullyLoaded: false, // True if all articles are loaded
  activeXhr: null // Active XHR retrieving a feed
};

/**
 * jQuery cache.
 */
r.feed.cache = {
  container: null,
  toolbar: null
};

/**
 * Reset feed related context.
 */
r.feed.reset = function() {
  // Hiding articles container
  r.feed.cache.container.hide();
  
  // Resetting highlights
  $('#subscriptions').find('li.active').removeClass('active');
};

/**
 * Initializing feed module.
 */
r.feed.init = function() {
  // jQuery cache
  r.feed.cache.container = $('#feed-container');
  r.feed.cache.toolbar = $('#toolbar');

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
    r.feed.cache.container.show();
    
    // Configuring contextual toolbar
    r.feed.cache.toolbar.find('> .feed').removeClass('hidden');
    
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
      r.feed.cache.toolbar.find('> .all').removeClass('hidden');
      
    } else if (target == 'unread') {
      // Configuring context for /all?unread=true
      r.feed.context.url = r.util.url.all;
      r.feed.context.unread = true;
      $('#unread-feed-button').addClass('active');
      
      // Specific toolbar actions for unread
      r.feed.cache.toolbar.find('> .unread').removeClass('hidden');
      
    } else if (target == 'starred') {
      // Configuring context for /starred
      r.feed.context.url = r.util.url.starred;
      r.feed.context.unread = false;
      $('#starred-feed-button').addClass('active');
      
      // Specific toolbar actions for starred
      r.feed.cache.toolbar.find('> .starred').removeClass('hidden');
      
    } else if (target.substring(0, 13) == 'subscription/') {
      // Configuring context for /subscription/id
      r.feed.context.url = '../api/' + target;
      r.feed.context.subscriptionId = target.substring(13);
      
      // Specific toolbar actions for subscriptions
      r.feed.cache.toolbar.find('> .subscription').removeClass('hidden');
      
    } else if (target.substring(0, 9) == 'category/') {
      // Configuring context for /category/id
      r.feed.context.url = '../api/' + target;
      r.feed.context.categoryId = target.substring(9);
      
      // Specific toolbar actions for categories
      r.feed.cache.toolbar.find('> .category').removeClass('hidden');
    } else if (target.substring(0, 7) == 'search/') {
      // Configuring context for /search/query
      r.feed.context.unread = false;
      r.feed.context.url = r.util.url.search.replace('{query}', target.substring(7));
      
      // Specific toolbar actions for search
      r.feed.cache.toolbar.find('> .search').removeClass('hidden');
    }
    
    // Focus on articles list
    r.feed.cache.container.focus();
    
    // Loading articles
    r.feed.load(false);
    
    // Loading subscriptions tree
    r.subscription.update();
  });
  
  // Smartphone and desktop scroll listener
  $(window).scroll(r.feed.scroll);
  r.feed.cache.container.scroll(r.feed.scroll);
  
  // Toolbar action: refresh
  r.feed.cache.toolbar.find('.refresh-button').click(function() {
    r.feed.load(false);
    r.subscription.update();
  });
  
  // Toolbar action: show all/new articles
  r.feed.cache.toolbar.find('.all-button').click(function() {
    r.feed.context.unread = !r.feed.context.unread;
    r.feed.context.unread ? $(this).html($.t('toolbar.showall')) : $(this).html($.t('toolbar.shownew'));
    r.feed.load(false);
  });
  
  // Toolbar action: mark all as read
  r.feed.cache.toolbar.find('.all-read-button').click(function() {
    r.feed.markAllRead();
  });
  
  // Toolbar action: list mode
  r.feed.cache.toolbar.find('.list-button').click(function() {
    r.user.setDisplayTitle(true);
    r.feed.updateMode(true);
  });
  
  // Toolbar action: full mode
  r.feed.cache.toolbar.find('.full-button').click(function() {
    r.user.setDisplayTitle(false);
    r.feed.updateMode(true);
  });

  // Toolbar action: narrow article
  r.feed.cache.toolbar.find('.narrow-article').click(function() {
    r.user.setNarrowArticle(!r.user.isNarrowArticle());
    r.feed.updateMode(true);
  });

  // Mark as read on a batch of articles
  r.feed.cache.container.on('click', '.markread', function() {
    r.article.read($(this).prevAll('.feed-item:not(.read)'), true);
  });
  
  // Update feed mode
  r.feed.updateMode(false);
};

/**
 * Feed scroll listener.
 */
r.feed.scroll = function() {
  var scroll = r.main.mobile ? $(window).scrollTop() : r.feed.cache.container.scrollTop();
  var height = r.main.mobile ? $(window).height() / 2 : r.feed.cache.container.height() / 2;
  var feedItemList = r.feed.cache.container.find('.feed-item');
  
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
    var itemsToRead = feedItemList.slice(0, selected.index('.feed-item') + 1);
    if (r.feed.cache.container.hasClass('list')) itemsToRead = itemsToRead.filter('.unfolded');
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
    r.feed.cache.container.html(r.util.buildLoader());
    
    // Updating show all/show new button
    r.feed.context.unread ?  r.feed.cache.toolbar.find('.all-button').html($.t('toolbar.showall'))
        : r.feed.cache.toolbar.find('.all-button').html($.t('toolbar.shownew'));
    
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
  var data = {
    unread: r.feed.context.unread,
    limit: r.feed.context.limit()
  };
  
  if (r.feed.context.lastItem) {
    data.after_article = r.feed.context.lastItem.attr('data-article-id');
  }
  
  // Special case for search (use offset paging)
  if (next && r.feed.context.url.substring(0, 11) == r.util.url.search.substring(0, 11)) {
    data.offset = r.feed.cache.container.find('.feed-item').length;
  }
  
  // Calling API
  r.feed.activeXhr = r.util.ajax({
    url: r.feed.context.url,
    type: 'GET',
    data: data,
    done: function(data) {
      var nbArticles = $(data.articles).length;
      
      // Pre article build
      if (!next) {
        r.feed.cache.container.html('');
        
        // Empty placeholder
        if (nbArticles == 0) {
          r.feed.cache.container.append(r.feed.buildEmpty());
        }
        
        // Adding bumper
        var bumper = r.feed.buildBumper(data);
        r.feed.context.bumper = bumper;
        r.feed.cache.container.append(bumper);
      }
      
      // All articles are loaded?
      r.feed.context.fullyLoaded = nbArticles == 0;
      
      // Building articles
      $(data.articles).each(function(i, article) {
        // Escape some fields
        article.subscription.title = r.util.escape(article.subscription.title);
        article.creator = r.util.escape(article.creator);
        
        // Build article
        var item = r.article.build(article);
        r.feed.context.bumper.before(item);
        
        // Store last item
        if(i == nbArticles - 1) {
          r.feed.context.lastItem = item;
        }
      });
      
      // Post article build
      if (r.feed.context.unread
          && nbArticles == r.feed.context.limit()) {
        // Mark previous articles as read button
        r.feed.context.lastItem.after('<div class="markread"><a>' + $.t('feed.markread') + '</a></div>');
      }

      if (!next) {
        // Scrolling to top
        r.feed.scrollTop(0);
        
        // Focus article list and redraw
        r.feed.cache.container
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
    fail: function(jqXHR, textStatus) {
      r.feed.context.loading = false;
      r.feed.activeXhr = null;
      r.feed.cache.container.find('.loader').hide();
      
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
  var empty = $('<div class="empty">' + message + '<br /><img src="images/rssman.png" /></div>');
  $(empty).click(function() {
    $(this).addClass('bounce').one('webkitAnimationEnd mozAnimationEnd MSAnimationEnd oanimationend animationend', function() {
      $(this).removeClass('bounce');
    })
  });
  return empty;
};

/**
 * Build feed bumper according to feed context.
 */
r.feed.buildBumper = function(data) {
  var html = $.t('feed.nomorearticles');
  
  // Build html (note that unread state is irrelevant for searching)
  if (r.feed.context.unread && r.feed.context.url.substring(0, 11) != r.util.url.search.substring(0, 11)) {
    if (data.subscription) {
      html = $.t('feed.subscriptionnomoreunreadarticles', { subscription: data.subscription.title });
    } else {
      html = $.t('feed.nomoreunreadarticles');
    }
    html += '<br /><a href="#" class="showall">' + $.t('feed.showall') + '</a>';
    if (r.user.isDisplayTitle()) {
      html += '<br /><a href="#" class="markallread">' + $.t('feed.markallread') + '</a>';
    }
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

  // Mark all as read link
  bumper.find('.markallread').click(function() {
    r.feed.markAllRead();
    return false;
  });

  bumper.css('height', ($(window).height() - 200) + 'px');
  
  return bumper;
};

/**
 * Optimize articles list by destructing article's DOM outside of the viewport.
 */
r.feed.optimize = $.debounce(350, function() {
  // It's not necessary to optimize in list mode, the content is not rendered
  if (r.feed.cache.container.hasClass('list')) {
    return;
  }
  
  // Delete content from articles largely outside
  r.feed.cache.container.find('.feed-item:largelyoutside')
    .addClass('destroyed')
    .height(function() {
      return $(this).height();
    })
    .html('');
  
  // Rebuild articles near the viewport
  r.feed.cache.container.find('.feed-item.destroyed:not(:largelyoutside)')
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
    r.feed.cache.container.animate({ scrollTop: top }, 200);
  } else {
    $('body, html').scrollTop(top);
    r.feed.cache.container.scrollTop(top);
  }
};

/**
 * Update feed mode (list or full, narrow or not) according to user preference.
 */
r.feed.updateMode = function(reload) {
  var list = r.user.isDisplayTitle();
  var narrow = r.user.isNarrowArticle();
  var narrowBtn = r.feed.cache.toolbar.find('.narrow-article');

  if (list) {
    r.feed.cache.container.addClass('list');
  } else {
    r.feed.cache.container.removeClass('list');
  }

  if (narrow) {
    r.feed.cache.container.addClass('narrow');
    narrowBtn.find('img:first').removeClass('hidden');
    narrowBtn.find('img:last').addClass('hidden');
  } else {
    r.feed.cache.container.removeClass('narrow');
    narrowBtn.find('img:first').addClass('hidden');
    narrowBtn.find('img:last').removeClass('hidden');
  }
  
  if (reload) {
    // Reload feed
    r.feed.load(false);
  }
};

/**
 * Mark all as read.
 */
r.feed.markAllRead = function() {
  r.util.ajax({
    url: r.feed.context.url + '/read',
    type: 'POST',
    always: function() {
      r.feed.load(false);
      r.subscription.update();
    }
  });
};
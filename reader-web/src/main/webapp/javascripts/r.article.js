/**
 * Initializing article module.
 */
r.article.init = function() {
  // Delegate on mark as unread checkboxes
  $('#feed-container').on('change', '.feed-item .feed-item-unread input', function() {
    var checked = $(this).is(':checked');
    var item = $(this).closest('.feed-item');
    
    if (checked) {
      item.addClass('forceunread');
      r.article.read(item, false);
    } else {
      item.removeClass('forceunread');
      r.article.read(item, true);
    }
  });
  
  // Delegate on star buttons
  $('#feed-container').on('click', '.feed-item .feed-item-star', function(e) {
    var item = $(this).closest('.feed-item');
    var article = item.data('article');
    article.is_starred = !article.is_starred;
    
    // Update local star
    if (article.is_starred) {
      $(this).addClass('starred');
    } else {
      $(this).removeClass('starred');
      
      // Remove item from articles list if in /starred context
      if (r.feed.context.url == r.util.url.starred) {
        r.feed.context.total--;
        item.remove();
      }
    }
    
    // Calling API
    r.util.ajax({
      url: r.util.url.starred_star.replace('{id}', article.id),
      type: article.is_starred ? 'PUT' : 'DELETE'
    });
    
    e.stopPropagation();
  });
  
  // Delegate on article collapsed header click
  $('#feed-container').on('click', '.feed-item .collapsed .container', function() {
    var item = $(this).closest('.feed-item');
    var container = item.parent();
    
    // Add or remove unfolded mode for article
    if (item.hasClass('unfolded')) {
      item.removeClass('unfolded');
      
      // Delete article description
      item.find('.feed-item-description').html('');
    } else {
      container.find('.feed-item.unfolded').removeClass('unfolded');
      item.addClass('unfolded');
      
      // Fill article description
      var article = item.data('article');
      item.find('.feed-item-description').html(article.description);
      
      // Scroll to the beginning of this article
      r.feed.scrollTop(item.position().top + container.scrollTop() + 1);
    }
  });
  
  // Delegate on article content click
  $('#feed-container').on('click', '.feed-item .header, .feed-item .content', function() {
    var item = $(this).closest('.feed-item');
    var container = item.parent();
      
    // Scroll to the beginning of this article
    if (!item.hasClass('selected')) {
      r.feed.scrollTop(item.position().top + container.scrollTop() + 1, true);
    }
  });
};

/**
 * Mark an article as read or unread.
 */
r.article.read = function(item, read) {
  var current = item.hasClass('read');
  
  // Do nothing if read state has not changed
  if (current == read) {
    return;
  }
  
  // Update item state
  var articleId = item.attr('data-article-id');
  read ? item.addClass('read') : item.removeClass('read');
  
  // Update tree unread counts
  var count = read ? -1 : -2;
  var article = item.data('article');
  var subscriptionId = article.subscription.id;
  var subscription = $('#subscription-list li.subscription[data-subscription-id="' + subscriptionId + '"]');
  r.subscription.updateUnreadCount(subscription, count); // Update article's subscription
  
  // Update parent categories (only 1)
  subscription.parents('li.category').each(function(i, category) {
    r.subscription.updateUnreadCount($(category), count);
  });
  
  // Update main unread item and title
  var count = r.subscription.updateUnreadCount($('#unread-feed-button'), count);
  r.subscription.updateTitle(count);
  
  // Calling API
  var url = read ? r.util.url.article_read : r.util.url.article_unread;
  r.util.ajax({
    url: url.replace('{id}', articleId),
    type: 'POST'
  });
};

/**
 * Build an article from server data.
 */
r.article.build = function(article, classes) {
  var item = $('#template .feed-item').clone();
  var date = new Date(article.date);
  
  // Remove collapsed container in full mode
  if (!r.user.isDisplayTitle()) {
    item.find('.collapsed').remove();
  }
  
  // Store server data in element
  item.data('article', article);
  
  // Article state
  item.attr('data-article-id', article.id);
  if (article.is_read) {
    item.addClass('read');
  }
  
  // Copy provided classes
  if (classes) {
    item.attr('class', classes);
  }
  
  // Articles fields
  var title = article.title;
  if (article.url) {
    title = '<a href="' + article.url + '" target="_blank">' + title + '</a>';
  }
  item.find('.feed-item-title').html(title);
  
  item.find('.feed-item-date')
    .html(date.toLocaleString())
    .attr('title', date.toISOString())
    .timeago();
  
  if (r.feed.context.subscriptionId == null) {
    item.find('.feed-item-subscription').html('on <a href="#/feed/subscription/' + article.subscription.id + '">' + article.subscription.title + '</a>');
  } else {
    item.find('.feed-item-subscription').remove();
  }
  
  if (article.creator) {
    item.find('.feed-item-creator').html('by ' + article.creator);
  }
  
  // In list mode, don't fill the description now
  if (!r.user.isDisplayTitle()) {
    item.find('.feed-item-description').html(article.description);
  }
  
  if (article.comment_count > 0) {
    var html = article.comment_count + ' comments';
    if (article.comment_url) {
      html = '<a href="' + article.comment_url + '" target="_blank">' + html + '</a>';
    }
    item.find('.feed-item-comments').html(html);
  }
  
  if (article.is_starred) {
    item.find('.feed-item-star').addClass('starred');
  }
  
  // Collapsed fields
  item.find('.feed-item-collapsed-subscription').html(article.subscription.title);
  item.find('.feed-item-collapsed-title').html(article.title);
  if (article.url) {
    item.find('.feed-item-collapsed-link').html('<a href="' + article.url + '" target="_blank"><img src="images/external.png" /></a>');
  }
  item.find('.feed-item-collapsed-description').html(article.description.replace(/(<([^>]+)>)/ig, '').substring(0, 200));
  
  // Mark as unread state
  if (item.hasClass('forceunread')) {
    item.find('.feed-item-unread input').attr('checked', 'checked');
  }
  
  return item;
};

/**
 * Returns article item top position.
 */
r.article.top = function(item, scroll) {
  var top = item[0].offsetTop - scroll;
  item.data('top', top);
  return top;
};
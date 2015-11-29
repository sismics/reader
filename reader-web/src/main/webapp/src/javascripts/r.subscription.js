/**
 * Initializing subscription module.
 */
r.subscription.init = function() {
  // Actionbar: displaying subscriptions
  $('#subscriptions-show-button, #subscriptions .close-button, #subscriptions-backdrop').click(function() {
    $('#subscriptions').fadeToggle('fast');
    $('#subscriptions-backdrop').fadeToggle('fast');
  });

  // Button for changing subscription tree unread state
  $('#subscription-unread-button').click(function() {
    var unread = !r.user.isDisplayUnread();
    r.user.setDisplayUnread(unread);
    r.subscription.update();
  });
  
  // Tip for adding subscription
  $('#subscription-add-button').qtip({
    content: { text: $('#qtip-subscription-add') },
    position: {
      my: 'top left',
      at: 'bottom center',
      effect: false,
      viewport: $(window),
      adjust: { method: 'shift' }
    },
    show: { event: 'click' },
    hide: { event: 'click unfocus' },
    style: { classes: 'qtip-light qtip-shadow' },
    events: {
      visible: function() { $('#subscription-url-input').focus(); }
    }
  });
  
  // Adding a subscription
  $('#subscription-submit-button').click(function() {
    var _this = $(this);
    var url = $('#subscription-url-input').val();
    
    // Validating form
    if (url != '') {
      // Disable button during the request to avoid double entries
      _this.attr('disabled', 'disabled');
      $('#subscriptions .ajax-loader').removeClass('hidden');
      
      // Closing tip
      $('#subscription-add-button').qtip('hide');
      
      // Calling API
      r.util.ajax({
        url: r.util.url.subscription_add,
        type: 'PUT',
        data: { url: url },
        done: function(data) {
          // Reseting form
          $('#qtip-subscription-add form')[0].reset();
          
          // Open newly added feed
          window.location.hash = '#/feed/subscription/' + data.id;
        },
        fail: function(jqxhr) {
          var data = JSON.parse(jqxhr.responseText);
          alert(data.message);
        },
        always: function() {
          // Enabing button
          _this.removeAttr('disabled');
          $('#subscriptions .ajax-loader').addClass('hidden');
        }
      });
    }
    
    // Prevent form submission
    return false;
  });
  
  // Initializing toolbar subscriptions actions
  r.subscription.initToolbar();
  
  // Refresh subscription tree every minutes
  setInterval(function() {
    // Check if no category or subscription edit qtip is opened
    if ($('body .qtip').find('.qtip-subscription-edit:visible, .qtip-category-edit:visible').length == 0) {
      r.subscription.update();
    }
  }, 60000);
  
  $('#subscriptions').on('click', 'li a', function() {
    // Force hashchange trigger if the user clicks on an already opened feed
    if (window.location.hash == $(this).attr('href')) {
      $.History.trigger();
    }
    
    // Hide subscriptions on mobile
    if(r.main.mobile) {
      $('#subscriptions').fadeOut('fast');
      $('#subscriptions-backdrop').fadeOut('fast');
    }
  });
};

/**
 * Updating subscriptions tree.
 */
r.subscription.update = function() {
  // Unread state
  var unread = r.user.isDisplayUnread();
  
  // Getting subscriptions
  r.util.ajax({
    url: r.util.url.subscription_list,
    data: { unread: unread },
    type: 'GET',
    done: function(data) {
      if ($(data.categories[0].categories).size() > 0 || $(data.categories[0].subscriptions).size() > 0) {
        // Building HTML tree
        var html = '<ul id="category-root" data-category-id="' + data.categories[0].id + '">';
        $(data.categories[0].categories).each(function(i, category) {
          // Adding sub-category
          var subscriptionsHtml = '<ul ' + (category.folded ? 'style="display: none;"' : '') + '>';
          if ($(category.subscriptions).length > 0) {
            // Adding subscriptions
            $(category.subscriptions).each(function(i, subscription) {
              subscriptionsHtml += r.subscription.buildSubscriptionItem(subscription);
            });
          }
          subscriptionsHtml += '</ul>';
          html += r.subscription.buildCategoryItem(category, subscriptionsHtml);
        });
        
        // Adding remaining subscriptions
        $(data.categories[0].subscriptions).each(function(i, subscription) {
          html += r.subscription.buildSubscriptionItem(subscription);
        });
        html += '&nbsp;</ul>';
        
        // Updating HTML and force redraw
        $('#subscription-list')
          .html(html)
          .redraw();
      } else {
        // Empty placeholder
        var html = '<p>' + $.t('subscription.empty') + '</p>';
        if (unread) {
          html = '<p>' + $.t('subscription.emptyunread') + '</p>'
            + '<p><a href="#">' + $.t('subscription.showall') + '</a></p>';
        }
        $('#subscription-list').html(html);
        $('#subscription-list p a').click(function() {
          r.user.setDisplayUnread(false);
          r.subscription.update();
        });
      }
      
      // Updating main unread item and title
      var unreadItem = $('#unread-feed-button');
      r.subscription.updateUnreadCount(unreadItem, data.unread_count);
      r.subscription.updateTitle(data.unread_count);
      
      // Initializing tree features
      r.subscription.initSorting(data.categories[0].id);
      r.subscription.initCollapsing();
      r.subscription.initEditing();
    }
  });
};

/**
 * Building subscription li.
 */
r.subscription.buildSubscriptionItem = function(subscription) {
  var unread = '<span class="unread-count" ' + (subscription.unread_count == 0 ? 'style="display: none;"' : '') + '>&nbsp;(<span class="count">' + subscription.unread_count + '</span>)</span>';
  
  var title = r.util.escape(subscription.title);
  return '<li id="subscription-' + subscription.id + '" data-subscription-id="' + subscription.id + '" data-subscription-url="' + subscription.url + '" ' +
    'class="subscription' + (r.feed.context.subscriptionId == subscription.id ? ' active' : '') + (subscription.unread_count > 0 ? ' unread' : '') + '">' +
    '<a href="#/feed/subscription/' + subscription.id + '" title="' + title + '"> <img src="' + r.util.url.subscription_favicon.replace('{id}', subscription.id) + '" /> ' +
    (subscription.sync_fail_count >= 5 ? '<img src="images/warning.png" title="' + $.t('subscription.syncfail') + '" />' : '') +
    '<span class="title">' + title + '</span>' + unread + '</a>' +
    '<div class="edit"></div>' +
    '</li>';
};

/**
 * Building category li.
 */
r.subscription.buildCategoryItem = function(category, subscriptionsHtml) {
  var unread = '<span class="unread-count" ' + (category.unread_count == 0 ? 'style="display: none;"' : '') + '>&nbsp;(<span class="count">' + category.unread_count + '</span>)</span>';
  
  var name = r.util.escape(category.name);
  return '<li id="category-' + category.id + '" data-category-id="' + category.id + '" ' +
    'class="category' + (r.feed.context.categoryId == category.id ? ' active' : '') + (category.unread_count > 0 ? ' unread' : '') + '">' +
    '<div class="collapse ' + (category.folded ? 'closed' : 'opened') + '"></div>' +
    '<a href="#/feed/category/' + category.id + '" title="' + name + '"> <img src="images/category.png" /> ' +
    '<span class="name">' + name + '</span>' + unread + '</a>' +
    '<div class="edit"></div>' + 
    subscriptionsHtml +
    '</li>';
};

/**
 * Adding sorting feature.
 */
r.subscription.initSorting = function(rootCategoryId) {
  $('#subscription-list ul').sortable({
    connectWith: '#subscription-list ul', // Can move items between lists
    revert: 100, // 100ms revert animation duration
    items: 'li', // Only li can be moved
    distance: 15, // Drag only after 15px mouse distance
    placeholder: 'placeholder', // Placeholder CSS class
    forcePlaceholderSize: true, // Otherwise placeholder is 1px height
    stop: function(event, ui) {
      // Category or subscription moved
      if (ui.item.hasClass('subscription')) {
        // Getting contextual parameters
        var subscriptionId = ui.item.attr('data-subscription-id');
        var order = ui.item.index() - ui.item.prevAll('li.category').length; // Substract categories, which are not part of the order
        var categoryId = ui.item.parent().parent().attr('data-category-id');
        if (ui.item.parent().attr('id') == 'category-root') {
          categoryId = rootCategoryId;
        }

        // Calling API
        r.util.ajax({
          url: r.util.url.subscription_update.replace('{id}', subscriptionId),
          data: { category: categoryId, order: order },
          type: 'POST',
          always: function() {
            // Full tree update needed to update unread counts
            r.subscription.update();
          }
        });
      } else if (ui.item.hasClass('category')) {
        // If the user drop a category not in the root category, cancel and warn
        if (ui.item.parent().attr('id') != 'category-root') {
          $('#subscription-list ul').sortable('cancel');
          $().toastmessage('showErrorToast', $.t('category.nonesting'));
          return;
        }
        
        // Getting contextual parameters
        var categoryId = ui.item.attr('data-category-id');
        var order = ui.item.index();
        
        // Calling API
        r.util.ajax({
          url: r.util.url.category_update.replace('{id}', categoryId),
          data: { order: order },
          type: 'POST',
          fail: function(jqxhr) {
            // In case of error, client is no more synced with server, perform full update
            r.subscription.update();
          }
        });
      }
    }
  }).disableSelection();
};

/**
 * Initializing collapsing feature.
 */
r.subscription.initCollapsing = function() {
  $('#subscription-list .collapse').click(function() {
    var parent = $(this).parent();
    var children = parent.find('> ul');
    var categoryId = parent.attr('data-category-id');
    children.toggle();
    $(this).toggleClass('opened').toggleClass('closed');
    
    // Calling API
    r.util.ajax({
      url: r.util.url.category_update.replace('{id}', categoryId),
      data: { folded: !children.is(':visible') },
      type: 'POST',
      fail: function(jqxhr) {
        // In case of error, client is no more synced with server, perform full update
        r.subscription.update();
      }
    });
  });
};

/**
 * Initializing editing feature.
 */
r.subscription.initEditing = function() {
  //Subscriptions editing
  $('#subscription-list li.subscription > .edit').each(function() {
    // Initializing edit popup
    var parent = $(this).parent();
    var _this = $(this);
    var subscriptionId = parent.attr('data-subscription-id');
    var content = $('#template .qtip-subscription-edit').clone();
    var infoContent = $('#template .qtip-subscription-edit-info').clone();
    var titleInput = content.find('.subscription-edit-title-input');
    titleInput.val(parent.find('> a .title').text().trim());
    
    // Calling API delete
    $('.subscription-edit-delete-button', content).click(function() {
      if (confirm($.t('subscription.edit.deleteconfirm'))) {
        r.util.ajax({
          url: r.util.url.subscription_delete.replace('{id}', subscriptionId),
          type: 'DELETE',
          always: function() {
            // Full tree refresh
            r.subscription.update();
            // Go to home
            window.location.hash = '#/feed/unread';
          }
        });
      }
    });
    
    // Calling API edit
    $('.subscription-edit-submit-button', content).click(function() {
      var title = titleInput.val();
      
      if (title != '') {
        r.util.ajax({
          url: r.util.url.subscription_update.replace('{id}', subscriptionId),
          data: { title: title },
          type: 'POST',
          always: function() {
            // Full tree refresh
            r.subscription.update();
          }
        });
      }

      // Prevent form submission
      return false;
    });

    // Opening informations popup
    $('.subscription-edit-info-button', content).click(function() {
      _this.qtip('hide');

      // Get feed informations
      r.util.ajax({
        url: r.util.url.subscription_get.replace('{id}', subscriptionId),
        data: { limit: 0 },
        type: 'GET',
        done: function(data) {
          var table = $('.subscription-edit-info-table tbody', infoContent);
          $('.title', table).html(r.util.escape(data.subscription.title));
          $('.feed_title', table).html(r.util.escape(data.subscription.feed_title));
          $('.url', table)
              .attr('href', data.subscription.url)
              .html(data.subscription.url);
          $('.rss_url', table)
              .attr('href', data.subscription.rss_url)
              .html(data.subscription.rss_url);
          $('.category_name', table).html(data.subscription.category_name);
          var date = moment(data.subscription.create_date);
          $('.create_date', table)
              .attr('title', date.format('L LT'))
              .html(date.fromNow());
        }
      });

      // Get latest synchronizations
      r.util.ajax({
        url: r.util.url.subscription_sync.replace('{id}', subscriptionId),
        type: 'GET',
        done: function(data) {
          var html = '';
          $(data.synchronizations).each(function(i, sync) {
            var date = moment(sync.create_date);
            html += '<tr>' +
                '<td title="' + (sync.message ? sync.message : '') + '">' + (sync.success ? $.t('feed.info.syncok') : $.t('feed.info.syncfail')) + '</td>' +
                '<td title="' + date.format('L LT') + '">' + date.fromNow() + '</td>' +
                '<td>' + sync.duration + 'ms</td>' +
                '</tr>';
          });
          $('.subscription-edit-info-synctable tbody', infoContent).html(html);
        }
      });
    });
    
    // Creating edit popup
    $(this).qtip({
      content: { text: content },
      position: {
        my: 'top right',
        at: 'bottom center',
        effect: false,
        viewport: $(window)
      },
      show: { event: 'click' },
      hide: { event: 'click unfocus' },
      style: { classes: 'qtip-light qtip-shadow' }
    });

    // Creation informations popup
    $('.subscription-edit-info-button', content).qtip({
      content: { text: infoContent },
      position: {
        my: 'center',
        at: 'center',
        target: $(document.body)
      },
      show: {
        modal: {
          on: true,
          blur: true,
          escape: true
        },
        event: 'click'
      },
      hide: { event: '' },
      style: { classes: 'qtip-light qtip-shadow' }
    });
  });
  
  // Categories editing
  $('#subscription-list li.category > .edit').each(function() {
    // Initializing edit popup
    var parent = $(this).parent();
    var categoryId = parent.attr('data-category-id');
    var content = $('#template .qtip-category-edit').clone();
    var nameInput = content.find('.category-edit-name-input');
    nameInput.val(parent.find('> a .name').text().trim());
    
    // Calling API delete
    $('.category-edit-delete-button', content).click(function() {
      if (confirm($.t('category.edit.deleteconfirm'))) {
        r.util.ajax({
          url: r.util.url.category_delete.replace('{id}', categoryId),
          type: 'DELETE',
          always: function() {
            // Full tree refresh
            r.subscription.update();
            // Go to home
            window.location.hash = '#/feed/unread';
          }
        });
      }
    });
    
    // Calling API edit
    $('.category-edit-submit-button', content).click(function() {
      var name = nameInput.val();
      
      if (name != '') {
        r.util.ajax({
          url: r.util.url.category_update.replace('{id}', categoryId),
          data: { name: name },
          type: 'POST',
          always: function() {
            // Full tree refresh
            r.subscription.update();
          }
        });
      }
      
      // Prevent form submission
      return false;
    });
    
    // Creating edit popup
    $(this).qtip({
      content: { text: content },
      position: {
        my: 'top right',
        at: 'bottom center',
        effect: false,
        viewport: $(window)
      },
      show: { event: 'click' },
      hide: { event: 'click unfocus' },
      style: { classes: 'qtip-light qtip-shadow' }
    });
  });
};

/**
 * Initialize subscription related toolbar actions.
 */
r.subscription.initToolbar = function() {
  // Toolbar action: change category
  var content = $('#template .qtip-change-category');
  content.on('click', 'li', function() {
    var categoryId = $(this).attr('data-category-id');
    var subscriptionId = r.feed.context.subscriptionId;
    
    // Calling API
    r.util.ajax({
      url: r.util.url.subscription_update.replace('{id}', subscriptionId),
      data: { category: categoryId },
      type: 'POST',
      always: function() {
        $('#toolbar .category-button').qtip('hide');
        r.subscription.update();
      }
    });
  });
  
  // Configuring change category tooltip
  $('#toolbar .category-button').qtip({
    content: { text: content },
    position: {
      my: 'top middle',
      at: 'bottom center',
      effect: false,
      viewport: $(window)
    },
    show: { event: 'click' },
    hide: { event: 'click unfocus' },
    style: { classes: 'qtip-light qtip-shadow' },
    events: {
      show: function(e, api) {
        // Current subscription category
        var subscriptionId = r.feed.context.subscriptionId;
        var categoryId = $('#subscription-list .subscription[data-subscription-id="' + subscriptionId + '"]')
          .closest('*[data-category-id]')
          .attr('data-category-id');
        
        // Loading categories on qtip opening
        content.html('<img src="images/loader.gif" />');
        r.util.ajax({
          url: r.util.url.category_list,
          type: 'GET',
          done: function(data) {
            var categories = data.categories[0].categories;
            html = '<ul><li data-category-id="' + data.categories[0].id + '">' + $.t('category.empty') + '</li>';
            $(categories).each(function(i, category) {
              html += '<li data-category-id="' + category.id + '">'
                + r.util.escape(category.name) + '</li>';
            });
            html += '</ul>';
            content.html(html);
            content.find('li[data-category-id="' + categoryId + '"]').addClass('active');
          }
        });
      }
    }
  });
};

/**
 * Update unread count of a tree item.
 * If count == -1, substract 1, if count == -2, add 1,
 * otherwise, force at this count.
 */
r.subscription.updateUnreadCount = function(item, count) {
  var countItem = item.find('> a .count');
  var current = parseInt(countItem.text());
  if (count == -1) {
    count = current - 1;
  } else if (count == -2) {
    count = current + 1;
  }
  
  if (count > 0) {
    item.addClass('unread')
      .find('> a .unread-count')
      .show();
  } else {
    item.removeClass('unread')
      .find('> a .unread-count')
      .hide();
  }
  
  countItem.html(count);
  return count;
};

/**
 * Update application title.
 */
r.subscription.updateTitle = function(count) {
  var title = $.t('app');
  if (count > 0) {
    title = '(' + count + ') ' + title;
  }
  $('title').html(title);
};
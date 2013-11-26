/**
 * Initializing shortcuts module.
 */
r.shortcuts.init = function() {
  $(document).on('keydown', function(e) {
    var activeTag = document.activeElement.tagName.toLowerCase();
    if (activeTag == 'input' || activeTag == 'textarea') {
      // User is typing, disable shortcuts
      return;
    }
    
    var active = false;
    
    switch (e.which) {
    case 74: // J key: next article
    case 75: // K key: previous article
    case 78: // N key : next article
    case 80: // P key : previous article
      var container = $('#feed-container');
      active = true;
      if (container.is(':visible')) {
        var selectedItem = container.find('.feed-item.selected');
        var newItem = null;
        
        // Find the new selected item
        if (selectedItem.length == 1) {
          if (e.which == 74 || e.which == 78) {
            newItem = selectedItem.nextAll('.feed-item').first();
          } else {
            newItem = selectedItem.prevAll('.feed-item').first();
          }
        } else {
          newItem = container.find('.feed-item:first');
        }
        
        // Select the new item
        if (newItem.length == 1) {
          if (newItem.find('.collapsed').length == 1) {
            // Open collapsed item in list mode
            newItem.find('.collapsed .container').trigger('click');
          } else {
            // Scroll to article otherwise
            r.feed.scrollTop(newItem.position().top + container.scrollTop() + 1, true);
          }
        }
      }
      break;
      
    case 77: // M key: mark as read
    case 83: // S key: star
      var container = $('#feed-container');
      active = true;
      if (container.is(':visible')) {
        var selectedItem = container.find('.feed-item.selected');
        if (selectedItem.length == 1) {
          if (e.which == 77) {
            var checkbox = selectedItem.find('.feed-item-unread input[type="checkbox"]');
            checkbox.prop('checked', !checkbox.is(':checked')).trigger('change');
          } else {
            selectedItem.find('.feed-item-star:first').click();
          }
        }
      }
      break;
      
    case 82: // R key: refresh feed
      var refreshButton = $('#toolbar .refresh-button');
      active = true;
      if (refreshButton.is(':visible')) {
        refreshButton.trigger('click');
      }
      break;
      
    case 86: // V key: open item
      var container = $('#feed-container');
      active = true;
      if (container.is(':visible')) {
        var selectedItem = container.find('.feed-item.selected');
        if (selectedItem.length == 1) {
          window.open(selectedItem.find('.feed-item-title a').attr('href'));
        }
      }
      break;
    }
    
    if (active) {
      e.preventDefault();
    }
  });
};

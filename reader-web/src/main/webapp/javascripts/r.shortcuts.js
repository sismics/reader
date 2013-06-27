/**
 * Initializing shortcuts module.
 */
r.shortcuts.init = function() {
  $(document).on('keydown', function(e) {
    var activeTag = document.activeElement.tagName.toLowerCase();
    if (activeTag == 'input' || activeTag == 'textarea') {
      // User is typing, disable shortcuts
    }
    
    switch (e.which) {
    case 74: // J key: next article
    case 75: // K key: previous article
      var container = $('#feed-container');
      if (container.is(':visible')) {
        var selectedItem = container.find('.feed-item.selected');
        var newItem = null;
        
        // Find the new selected item
        if (selectedItem.length == 1) {
          if (e.which == 74) {
            newItem = selectedItem.next('.feed-item');
          } else {
            newItem = selectedItem.prev('.feed-item');
          }
        } else {
          newItem = container.find('.feed-item:first');
        }
        
        // Select the new item
        if (newItem.length == 1) {
          r.feed.scrollTop(newItem.position().top + container.scrollTop() + 1, true);
        }
      }
      break;
      
    case 77: // M key: mark as read
    case 83: // S key: star
      var container = $('#feed-container');
      if (container.is(':visible')) {
        var selectedItem = container.find('.feed-item.selected');
        if (selectedItem.length == 1) {
          if (e.which == 77) {
            var checkbox = selectedItem.find('.feed-item-unread input[type="checkbox"]');
            console.log(checkbox.is(':checked'));
            checkbox.prop('checked', !checkbox.is(':checked')).trigger('change');
          } else {
            selectedItem.find('.feed-item-star').click();
          }
        }
      }
      break;
      
    case 82: // R key: refresh feed
      var refreshButton = $('#toolbar .refresh-button');
      if (refreshButton.is(':visible')) {
        refreshButton.trigger('click');
      }
      break;
      
    case 86: // V key: open item
      var container = $('#feed-container');
      if (container.is(':visible')) {
        var selectedItem = container.find('.feed-item.selected');
        if (selectedItem.length == 1) {
          selectedItem.find('.feed-item-title a').trigger('click');
        }
      }
      break;
    }
  });
};
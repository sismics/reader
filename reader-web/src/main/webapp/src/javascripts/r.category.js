/**
 * Initializing category module.
 */
r.category.init = function() {
  // Tip for adding category
  $('#category-add-button').qtip({
    content: { text: $('#qtip-category-add') },
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
  
  // Adding a category
  $('#category-submit-button').click(function() {
    var _this = $(this);
    var name = $('#category-name-input').val();
    
    // Validating form
    if (name != '') {
      // Disable button during the request to avoid double entries
      _this.attr('disabled', 'disabled');
      
      // Calling API
      r.util.ajax({
        url: r.util.url.category_add,
        type: 'PUT',
        data: { name: name },
        done: function(data) {
          // Closing tip
          $('#category-add-button').qtip('hide');
          
          // Reseting form
          $('#qtip-category-add form')[0].reset();
          
          // Updating subscriptions tree
          r.subscription.update();
        },
        always: function() {
          // Enabing button
          _this.removeAttr('disabled');
        }
      });
    }
    
    // Prevent form submission
    return false;
  });
};
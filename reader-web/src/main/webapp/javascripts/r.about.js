/**
 * Reset about related context.
 */
r.about.reset = function() {
  // Hiding about container
  $('#about-container').hide();
};

/**
 * Initializing about module.
 */
r.about.init = function() {
  // Listening hash changes on #/about/*
  // /about/
  $.History.bind('/about/', function(state, target) {
    // Resetting page context
    r.main.reset();
    
    // Showing about container
    $('#about-container').show();
    
    // Configuring contextual toolbar
    $('#toolbar > .about').removeClass('hidden');
    
    // Loading logs
    r.about.loadLogs(false);
  });
  
  // Rebuild index button click
  $('#about-container .rebuild-index-button').click(function() {
    r.util.ajax({
      url: r.util.url.app_batch_reindex,
      type: 'POST',
      done: function(data) {
        $().toastmessage('showSuccessToast', $.t('about.rebuildindex.success'));
      }
    });
  });
};

/**
 * Load logs.
 */
r.about.loadLogs = function(next) {
  r.util.ajax({
    url: r.util.url.app_log,
    type: 'GET',
    done: function(data) {
      var html = data;
      $('#logs-container').html(html);
    }
  });
};

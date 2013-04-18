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
  
  // Logs infinite scrolling
  $('#logs-container').scroll(function() {
    if ($('#logs-container .log-item:last').visible(true)) {
      r.about.loadLogs(true);
    }
  });
  
  // Reload logs on level change
  $('#logs-level-select').change(function() {
    r.about.loadLogs(false);
  });
};

/**
 * Load logs.
 */
r.about.loadLogs = function(next) {
  var count = 0;
  if (next) {
    var total = $('#logs-container').data('total');
    count = $('#logs-container .log-item').length;
    if (count >= total) {
      return;
    }
  }
  
  r.util.ajax({
    url: r.util.url.app_log,
    type: 'GET',
    data: { limit: 100, offset: next ? count : 0, level: $('#logs-level-select').val() },
    done: function(data) {
      var html = '';
      $(data.logs).each(function(i, log) {
        html += '<div class="log-item ' + log.level.toLowerCase() + '"><div class="date">' + log.date + '</div><div class="level">' + log.level + '</div><div class="tag">' + log.tag + '</div><div class="message">' + log.message + '</div></div>';
      });
      
      if (next) {
        $('#logs-container').append(html);
      } else {
        $('#logs-container').html(html);
        $('#logs-container').data('total', data.total)
      }
    }
  });
};

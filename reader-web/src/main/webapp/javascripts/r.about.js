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
    
    if (r.user.userInfo.is_admin) {
      // Loading logs
      r.about.loadLogs(false);
    } else {
      // User is not admin, hide related features
      $('#about-container .admin').hide();
    }
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
    if ($('#logs-table tr.log-item:last').visible(true)) {
      r.about.loadLogs(true);
    }
  });
  
  // Reload logs on level change
  $('#logs-level-select').change(function() {
    r.about.loadLogs(false);
  });
  
  // Reload logs on refresh button click
  $('#logs-refresh-button').click(function() {
    r.about.loadLogs(false);
  });
};

/**
 * Load logs.
 */
r.about.logsLoading = false;
r.about.loadLogs = function(next) {
  // Stop if already loading something
  if (r.about.logsLoading) {
    return;
  }
  
  // Check if there is more to load
  var count = 0;
  if (next) {
    var total = $('#logs-table').data('total');
    count = $('#logs-table tr.log-item').length;
    if (count >= total) {
      return;
    }
  }
  
  // Calling API
  r.about.logsLoading = true;
  r.util.ajax({
    url: r.util.url.app_log,
    type: 'GET',
    data: { limit: 100, offset: next ? count : 0, level: $('#logs-level-select').val() },
    done: function(data) {
      // Building table rows
      var html = '';
      $(data.logs).each(function(i, log) {
        html += '<tr class="log-item ' + log.level.toLowerCase() + '">'
            + '<td class="date">' + moment(log.date).format('YYYY-MM-DD HH:mm:ss') + '</td>'
            + '<td class="level">' + log.level + '</td>'
            + '<td class="tag">' + log.tag + '</td>'
            + '<td class="message">' + log.message + '</td>'
          '</tr>';
      });
      
      // Add or replace new rows
      if (next) {
        $('#logs-table').append(html);
      } else {
        $('#logs-table').html(html);
        $('#logs-table').data('total', data.total)
      }
    },
    always: function() {
      r.about.logsLoading = false;
    }
  });
};

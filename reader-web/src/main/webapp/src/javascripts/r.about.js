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
    
    if (r.user.hasBaseFunction('ADMIN')) {
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
  
  // Current application version
  r.util.ajax({
    url: r.util.url.app_version,
    type: 'GET',
    done: function(data) {
      var currentVersion = data.current_version;
      
      // Populate application informations
      $('#about-version').html(currentVersion);
      $('#about-used-memory').html(numeral(data.total_memory - data.free_memory).format('0b'));
      $('#about-total-memory').html(numeral(data.total_memory).format('0b'));

      // Get cookie
      $.cookie.json = true;
      var cookie = $.cookie('update_check');
      $.cookie.json = false;
      
      if (cookie === undefined) {
        // Last version from GitHub
        r.util.ajax({
          url: r.util.url.github_tags,
          type: 'GET',
          dataType: 'jsonp',
          done: function(data) {
            var tag = data[0];
            
            if (tag) {
              // Fetch commit data
              r.util.ajax({
                url: tag.commit.url,
                type: 'GET',
                dataType: 'jsonp',
                done: function(commit) {
                  r.about.showUpdate(currentVersion, tag.name, commit.commit.author.date);
                  
                  // Set cookie
                  $.cookie.json = true;
                  $.cookie('update_check', { 'tag': tag.name, 'date': commit.commit.author.date }, { expires: 1 });
                  $.cookie.json = false;
                },
                fail: function() {} // Ignore failing
              });
            }
          },
          fail: function() {} // Ignore failing
        });
      } else {
        // Show update with cached GitHub data
        r.about.showUpdate(currentVersion, cookie.tag, cookie.date);
      }
    }
  });
};

/**
 * Show update label if needed.
 */
r.about.showUpdate = function(currentVersion, tag, tagDate) {
  var date = moment(tagDate);
  var diff = moment().diff(date);
  
  
  if (diff > 3600000 * 24 && r.about.normalizeTag(currentVersion) < r.about.normalizeTag(tag)) {
    $('#subscriptions .update, #about-version-new')
      .show()
      .html('<a href="http://www.sismics.com/reader/" target="_blank">' + $.t('about.newupdate') + ': ' + tag + '</a>');
  }
};

/**
 * Transform a tag in int value.
 */
r.about.normalizeTag = function(tag) {
  var out = parseInt(tag.replace(/\./g, ''));
  if (out < 10) out *= 100;
  if (out < 100) out *= 10;
  return out;
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

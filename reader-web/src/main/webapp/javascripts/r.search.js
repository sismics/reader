/**
 * Initializing search module.
 */
r.search.init = function() {
  // Search on press enter
  $('#search-input').keydown(function(e) {
    if (e.which != 13) {
      return;
    }
    
    var query = $('#search-input').val();
    if (query != '') {
      window.location.hash = '#/feed/search/' + encodeURI(query);
    }
  });
};
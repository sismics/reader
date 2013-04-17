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
  });
};

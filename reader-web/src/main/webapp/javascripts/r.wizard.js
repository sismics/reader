/**
 * Reset wizard related context.
 */
r.wizard.reset = function() {
  // Hiding wizard container
  $('#wizard-container').hide();
};

/**
 * Initializing wizard module.
 */
r.wizard.init = function() {
  // Listening hash changes on #/wizard/*
  // /wizard/
  $.History.bind('/wizard/', function(state, target) {
    // Resetting page context
    r.main.reset();
    
    // Showing settings container
    $('#wizard-container').show();
    
    // Configuring contextual toolbar
    $('#toolbar > .wizard').removeClass('hidden');
  });
};

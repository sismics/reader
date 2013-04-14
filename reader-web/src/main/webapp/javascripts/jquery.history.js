/**
 * @depends nothing
 * @name core.console
 * @package jquery-sparkle {@link http://balupton.com/projects/jquery-sparkle}
 */

/**
 * Console Emulator
 * We have to convert arguments into arrays, and do this explicitly as webkit (chrome) hates function references, and arguments cannot be passed as is
 * @version 1.0.3
 * @date August 31, 2010
 * @since 0.1.0-dev, December 01, 2009
 * @package jquery-sparkle {@link http://balupton.com/projects/jquery-sparkle}
 * @author Benjamin "balupton" Lupton {@link http://balupton.com}
 * @copyright (c) 2009-2010 Benjamin Arthur Lupton {@link http://balupton.com}
 * @license MIT License {@link http://creativecommons.org/licenses/MIT/}
 */

// Check to see if console exists, if not define it
if ( typeof window.console === 'undefined' ) {
  window.console = {};
}

// Check to see if we have emulated the console yet
if ( typeof window.console.emulated === 'undefined' ) {
  // Emulate Log
  if ( typeof window.console.log === 'function' ) {
    window.console.hasLog = true;
  }
  else {
    if ( typeof window.console.log === 'undefined' ) {
      window.console.log = function(){};
    }
    window.console.hasLog = false;
  }

  // Emulate Debug
  if ( typeof window.console.debug === 'function' ) {
    window.console.hasDebug = true;
  }
  else {
    if ( typeof window.console.debug === 'undefined' ) {
      window.console.debug = !window.console.hasLog ? function(){} : function(){
        var arr = ['console.debug:']; for(var i = 0; i < arguments.length; i++) { arr.push(arguments[i]); };
          window.console.log.apply(window.console, arr);
      };
    }
    window.console.hasDebug = false;
  }

  // Emulate Warn
  if ( typeof window.console.warn === 'function' ) {
    window.console.hasWarn = true;
  }
  else {
    if ( typeof window.console.warn === 'undefined' ) {
      window.console.warn = !window.console.hasLog ? function(){} : function(){
        var arr = ['console.warn:']; for(var i = 0; i < arguments.length; i++) { arr.push(arguments[i]); };
          window.console.log.apply(window.console, arr);
      };
    }
    window.console.hasWarn = false;
  }

  // Emulate Error
  if ( typeof window.console.error === 'function' ) {
    window.console.hasError = true;
  }
  else {
    if ( typeof window.console.error === 'undefined' ) {
      window.console.error = function(){
        var msg = "An error has occured.";

        // Log
        if ( window.console.hasLog ) {
          var arr = ['console.error:']; for(var i = 0; i < arguments.length; i++) { arr.push(arguments[i]); };
            window.console.log.apply(window.console, arr);
          // Adjust Message
          msg = 'An error has occured. More information is available in your browser\'s javascript console.'
        }

        // Prepare Arguments
        for ( var i = 0; i < arguments.length; ++i ) {
          if ( typeof arguments[i] !== 'string' ) {
            break;
          }
          msg += "\n"+arguments[i];
        }

        // Throw Error
        if ( typeof Error !== 'undefined' ) {
          throw new Error(msg);
        }
        else {
          throw(msg);
        }
      };
    }
    window.console.hasError = false;
  }

  // Emulate Trace
  if ( typeof window.console.trace === 'function' ) {
    window.console.hasTrace = true;
  }
  else {
    if ( typeof window.console.trace === 'undefined' ) {
      window.console.trace = function(){
        window.console.error('console.trace does not exist');
      };
    }
    window.console.hasTrace = false;
  }

  // Done
  window.console.emulated = true;
}
/**
 * @depends jquery, core.console
 * @name jquery.history
 * @package jquery-history {@link http://balupton.com/projects/jquery-history}
 */

// Start of our jQuery Plugin
(function($)
{ // Create our Plugin function, with $ as the argument (we pass the jQuery object over later)
  // More info: http://docs.jquery.com/Plugins/Authoring#Custom_Alias

  /**
   * jQuery History
   * @version 1.5.0
   * @date August 31, 2010
   * @since 0.1.0-dev, July 24, 2008
     * @package jquery-history {@link http://balupton.com/projects/jquery-history}
   * @author Benjamin "balupton" Lupton {@link http://balupton.com}
   * @copyright (c) 2008-2010 Benjamin Arthur Lupton {@link http://balupton.com}
   * @license MIT License {@link http://creativecommons.org/licenses/MIT/}
   */
  // Check our class exists
  if ( !($.History||false) ) {
    // Declare our class
    $.History = {
      // Our Plugin definition

      // -----------------
      // Options

      options: {
        debug: false
      },

      // -----------------
      // Variables

      state:    '',
      $window:  null,
      $iframe:  null,
      handlers: {
        generic:  [],
        specific: {}
      },

      // --------------------------------------------------
      // Functions

      /**
       * Extract the Hash from a URL
       * @param {String} hash
       */
      extractHash: function ( url ) {
        // Extract the hash
        var hash = url
          .replace(/^[^#]*#/, '') /* strip anything before the first anchor */
          .replace(/^#+|#+$/, '')
          ;

        // Return hash
        return hash;
      },

      /**
       * Get the current state of the application
       */
          getState: function ( ) {
        var History = $.History;

        // Get the current state
        return History.state;
          },
      /**
       * Set the current state of the application
       * @param {String} hash
       */
      setState: function ( state ) {
        var History = $.History;

        // Format the state
        state = History.extractHash(state)

        // Apply the state
        History.state = state;

        // Return the state
        return History.state;
      },

      /**
       * Get the current hash of the browser
       */
      getHash: function ( ) {
        var History = $.History;

        // Get the hash
        var hash = History.extractHash(window.location.hash || location.hash);

        // Return the hash
        return hash;
      },

      /**
       * Set the current hash of the browser and iframe if present
       * @param {String} hash
       */
      setHash: function ( hash ) {
        var History = $.History;

        // Prepare hash
        hash = History.extractHash(hash);

        // Write hash
        if ( typeof window.location.hash !== 'undefined' ) {
          if ( window.location.hash !== hash ) {
            window.location.hash = hash;
          }
        } else if ( location.hash !== hash ) {
          location.hash = hash;
        }

        // Done
        return hash;
      },

      /**
       * Go to the specific state - does not force a history entry like setHash
       * @param {String} to
       */
      go: function ( to ) {
        var History = $.History;

        // Format
        to = History.extractHash(to);

        // Get current
        var hash = History.getHash(),
          state = History.getState();

        // Has the hash changed
        if ( to !== hash ) {
          // Yes, update the hash
          // And wait for the next automatic fire
          History.setHash(to);
        } else {
          // Has the state changed?
          if ( to !== state ) {
            // Yes, Update the state
            History.setState(to);
            
            // Trigger our change
            History.trigger();
          }
        }

        // Done
        return true;
      },

      /**
       * Handle when the hash has changed
       * @param {Event} e
       */
      hashchange: function ( e ) {
        var History = $.History;

        // Get Hash
        var hash = History.getHash();

        // Handle the new hash
        History.go(hash);

        // All done
        return true;
      },

      /**
       * Bind a handler to a hash
       * @param {Object} state
       * @param {Object} handler
       */
      bind: function ( state, handler ) {
        var History = $.History;

        // Handle
        if ( handler ) {
          // We have a state specific handler
          // Prepare
          if ( typeof History.handlers.specific[state] === 'undefined' ) {
            // Make it an array
            History.handlers.specific[state] = [];
          }
          // Push new handler
          History.handlers.specific[state].push(handler);
        }
        else {
          // We have a generic handler
          handler = state;
          History.handlers.generic.push(handler);
        }

        // Done
        return true;
      },

      /**
       * Trigger a handler for a state
       * @param {String} state
       */
      trigger: function ( state ) {
        var History = $.History;

        // Prepare
        if ( typeof state === 'undefined' ) {
          // Use current
          state = History.getState();
        }
        var i, n, handler, list;

        // Fire specific (matching by starting string)
        for (var s in History.handlers.specific) {
          if (state.indexOf(s) == 0) {
            var remaining = state.substr(s.length);
            list = History.handlers.specific[s];
            for ( i = 0, n = list.length; i < n; ++i ) {
              // Fire the specific handler
              handler = list[i];
              handler(state, remaining);
            }
          }
        }
        

        // Fire generics
        list = History.handlers.generic;
        for ( i = 0, n = list.length; i < n; ++i ) {
          // Fire the specific handler
          handler = list[i];
          handler(state);
        }

        // Done
        return true;
      },

      // --------------------------------------------------
      // Constructors

      /**
       * Construct our application
       */
      construct: function ( ) {
        var History = $.History;

        // Modify the document
        $(document).ready(function() {
          // Prepare the document
          History.domReady();
        });

        // Done
        return true;
      },

      /**
       * Configure our application
       * @param {Object} options
       */
      configure: function ( options ) {
        var History = $.History;

        // Set options
        History.options = $.extend(History.options, options);

        // Done
        return true;
      },

      domReadied: false,
      domReady: function ( ) {
        var History = $.History;

        // Runonce
        if ( History.domRedied ) {
          return;
        }
        History.domRedied = true;

        // Define window
        History.$window = $(window);

        // Apply the hashchange function
        History.$window.bind('hashchange', this.hashchange);

        // Fire the initial after init
        setTimeout(function() {
          var hash = History.getHash();
          if ( hash ) {
            History.$window.trigger('hashchange');
          }
        }, 200)
        
        // All done
        return true;
      }
    }; // We have finished extending/defining our Plugin

    // --------------------------------------------------
    // Finish up

    // Instantiate
    $.History.construct();
  }
  else {
    window.console.warn('$.History has already been defined...');
  }

  // Finished definition
})(jQuery); // We are done with our plugin, so lets call it with jQuery as the argument

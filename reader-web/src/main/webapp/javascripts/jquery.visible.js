(function($){

    /**
     * Copyright 2012, Digital Fusion
     * Licensed under the MIT license.
     * http://teamdf.com/jquery-plugins/license/
     *
     * @author Sam Sehnert
     * @desc A small plugin that checks whether elements are within
     *       the user visible viewport of a web browser.
     *       only accounts for vertical position, not horizontal.
     */
    $.fn.visible = function(partial){
        if ($(this).size() == 0) {
          return false;
        }
        
        var $t              = $(this),
            $w              = $(window),
            viewTop         = $w.scrollTop(),
            viewBottom      = viewTop + $w.height(),
            _top            = $t.offset().top,
            _bottom         = _top + $t.height(),
            compareTop      = partial === true ? _bottom : _top,
            compareBottom   = partial === true ? _top : _bottom;
        
        return ((compareBottom <= viewBottom) && (compareTop >= viewTop));
    };
    
    /**
     * JQuery selector returning elements largely outside the window.
     */
    $.extend($.expr[':'], {
      largelyoutside: function(el) {
        var wheight = $(window).height();
        var offset = $(el).offset();
        var wscrolltop = $(window).scrollTop();
        return offset.top > wscrolltop + wheight * 4 || offset.top + $(el).height() < wscrolltop - wheight * 4;
      }
    });
    
})(jQuery);
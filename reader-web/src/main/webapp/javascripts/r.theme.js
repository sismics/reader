/**
 * Initialize theme module.
 */
r.theme.init = function() {
  r.theme.update(r.user.userInfo.theme);
};

/**
 * Update current theme.
 */
r.theme.update = function(theme) {
  // Remove previous custom theme
  less.sheets = $.grep(less.sheets, function(sheet) {
    return !$(sheet).hasClass('custom');
  });
  
  $('html > style').remove();
    
  if (theme) {
    // Add custom stylesheet
    less.sheets.push($('<link class="custom" rel="stylesheet/less" href="stylesheets/theme/' + theme + '" type="text/css" />')[0]);
  }
  
  less.refresh();
};
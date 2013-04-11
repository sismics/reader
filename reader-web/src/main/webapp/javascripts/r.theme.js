/**
 * Initialize theme module.
 */
r.theme.init = function() {
  r.user.userInfo.theme = 'highcontrast'; // TODO Use parameter
  if (r.user.userInfo.theme) {
    less.sheets.push($('<link rel="stylesheet/less" href="stylesheets/theme/' + r.user.userInfo.theme + '.less" type="text/css"  />')[0]);
    less.refresh();
  }
};
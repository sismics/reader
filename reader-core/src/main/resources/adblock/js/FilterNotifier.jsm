/*
 * This file is part of Adblock Plus <http://adblockplus.org/>,
 * Copyright (C) 2006-2013 Eyeo GmbH
 *
 * Adblock Plus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as
 * published by the Free Software Foundation.
 *
 * Adblock Plus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Adblock Plus.  If not, see <http://www.gnu.org/licenses/>.
 */


//
// This file has been generated automatically from Adblock Plus source code
//

(function (_patchFunc1) {
  var listeners = [];
  var FilterNotifier = {
    addListener: function (listener) {
      if (listeners.indexOf(listener) >= 0)
        return ;
      listeners.push(listener);
    }
    ,
    removeListener: function (listener) {
      var index = listeners.indexOf(listener);
      if (index >= 0)
        listeners.splice(index, 1);
    }
    ,
    triggerListeners: function (action, item, param1, param2, param3) {
      for (var _loopIndex0 = 0;
      _loopIndex0 < listeners.length; ++ _loopIndex0) {
        var listener = listeners[_loopIndex0];
        listener(action, item, param1, param2, param3);
      }
    }
    
  };
  if (typeof _patchFunc1 != "undefined")
    eval("(" + _patchFunc1.toString() + ")()");
  window.FilterNotifier = FilterNotifier;
}
)(window.FilterNotifierPatch);

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

XMLHttpRequest = function()
{
  this.headers = {};
  this.responseHeaders = {};
  this.progressEventListeners = [];
  this.loadendEventListeners = [];
  this.loadEventListeners = [];
  this.errorEventListeners = [];
  this.abortEventListeners = [];
  this.aborted = false;
  this.async = true;
  this.readyState = XMLHttpRequest.UNSENT;
  this.responseText = "";
  this.status = 0;
};

XMLHttpRequest.UNSENT = 0;
XMLHttpRequest.OPEN = 1;
XMLHttpRequest.HEADERS_RECEIVED = 2;
XMLHttpRequest.LOADING = 3;
XMLHttpRequest.DONE = 4;

XMLHttpRequest.prototype =
{
  open: function(method, url, async, user, password)
  {
    this.async = (typeof async == "undefined" || async) ? true : false;
    this.method = method || "GET";
    this.url = url;
    this.readyState = XMLHttpRequest.OPEN;
    this.onreadystatechange();
  },
  setRequestHeader: function(header, value)
  {
    this.headers[header] = value;
  },
  send: function(data)
  {
    var self = this;

    Android.httpSend(self.method, self.url, self.headers, self.async, handleResponse);

    function handleResponse(code, message, headers, text)
    {
      if (self.aborted)
        return;
      if (headers != null)
      {
        for (var i = 0; i < headers.length; i++)
        {
          var headerName = headers[i][0];
          var headerValue = headers[i][1];
          if (headerName)
            self.responseHeaders[headerName] = headerValue;
        }
      }
      self.readyState = XMLHttpRequest.HEADERS_RECEIVED;
      self.onreadystatechange();

      self.readyState = XMLHttpRequest.LOADING;
      self.onreadystatechange();
      self.status = parseInt(code) || undefined;
      self.statusText = message || "";

      self.responseText = text;

      self.readyState = XMLHttpRequest.DONE;
      self.onreadystatechange();
      self.triggerListeners("load");
      self.triggerListeners("loadend");
    }
  },
  abort: function()
  {
    this.aborted = true;
    this.triggerListeners("abort");
    this.readyState = XMLHttpRequest.DONE;
    this.onreadystatechange();
  },
  onreadystatechange: function(){},
  getResponseHeader: function(header)
  {
    if (this.readyState < XMLHttpRequest.LOADING)
      throw new Error("INVALID_STATE_ERR");
    else
    {
      var lcHeader = header.toLowerCase();
      var returnedHeaders = [];
      for (var rHeader in this.responseHeaders)
      {
        if (this.responseHeaders.hasOwnProperty(rHeader) && rHeader.toLowerCase() == lcHeader)
          returnedHeaders.push(this.responseHeaders[rHeader]);
      }

      if (returnedHeaders.length)
        return returnedHeaders.join(", ");
    }

    return null;
  },
  getAllResponseHeaders: function(header)
  {
    if (this.readyState < 3)
      throw new Error("INVALID_STATE_ERR");
    else
    {
      var returnedHeaders = [];

      for (var header in this.responseHeaders)
        returnedHeaders.push(header + ": " + this.responseHeaders[header]);

      return returnedHeaders.join("\r\n");
    }
  },
  overrideMimeType: function(mime) {},
  addEventListener: function(type, listener, useCapture)
  {
    var listeners = null;

    if (type == "progress")
      listeners = this.progressEventListeners;
    else if (type == "loadend")
      listeners = this.loadendEventListeners;
    else if (type == "load")
      listeners = this.loadEventListeners;
    else if (type == "error")
      listeners = this.errorEventListeners;
    else if (type == "abort")
      listeners = this.abortEventListeners;

    if (listeners == null || listeners.indexOf(listener) >= 0)
        return;
    listeners.push(listener);
  },
  triggerListeners: function(type)
  {
    var listeners = null;

    if (type == "progress")
      listeners = this.progressEventListeners;
    else if (type == "loadend")
      listeners = this.loadendEventListeners;
    else if (type == "load")
      listeners = this.loadEventListeners;
    else if (type == "error")
      listeners = this.errorEventListeners;
    else if (type == "abort")
      listeners = this.abortEventListeners;

    for (var i = 0; i < listeners.length; i++)
    {
      var listener = listeners[i];
      listener();
    }
  }
};

XMLHttpRequest.prototype.channel =
{
  status: -1,
  notificationCallbacks: {},
  loadFlags: 0,
  INHIBIT_CACHING: 0,
  VALIDATE_ALWAYS: 0,
  QueryInterface: function()
  {
    return this;
  }
};

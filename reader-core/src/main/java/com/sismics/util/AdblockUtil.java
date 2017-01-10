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

package com.sismics.util;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.sismics.util.adblock.Helper;
import com.sismics.util.adblock.JSEngine;
import com.sismics.util.adblock.Subscription;
import com.sismics.util.adblock.SubscriptionParser;
import org.apache.commons.lang.StringEscapeUtils;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.ScriptException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdblockUtil {
    
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(AdblockUtil.class);
    
    private List<Subscription> subscriptions;

    private JSEngine js;

    private boolean interactive;
    
    public void start() throws Exception {
        interactive = false;
        js = new JSEngine();
        URL url = Resources.getResource("adblock" + File.separator + "js" + File.separator + "start.js");
        js.put("_locale", Locale.getDefault().toString());
        js.put("_datapath", "");
        js.put("_separator", File.separator);
        js.put("_version", "");
        js.put("Android", new Helper(js));
        js.evaluate(Resources.toString(url, Charsets.UTF_8));
    }
    
    /**
     * Returns list of known subscriptions.
     */
    public List<Subscription> getSubscriptions() {
        if (subscriptions == null) {
            subscriptions = new ArrayList<Subscription>();

            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser;
            try {
                parser = factory.newSAXParser();
                parser.parse(AdblockUtil.class.getResourceAsStream("/adblock/subscriptions.xml"), new SubscriptionParser(subscriptions));
            } catch (Exception e) {
                log.error("Error parsing subscriptions", e);
            }
        }
        return subscriptions;
    }

    /**
     * Returns subscription information.
     * 
     * @param url
     *            subscription url
     */
    public Subscription getSubscription(String url) {
        List<Subscription> subscriptions = getSubscriptions();

        for (Subscription subscription : subscriptions) {
            if (subscription.url.equals(url))
                return subscription;
        }
        return null;
    }

    /**
     * Adds provided subscription and removes previous subscriptions if any.
     * 
     * @param subscription Subscription to add
     */
    public void setSubscription(Subscription subscription) throws Exception {
        if (subscription != null) {
            final JSONObject jsonSub = new JSONObject();
            jsonSub.put("url", subscription.url);
            jsonSub.put("title", subscription.title);
            jsonSub.put("homepage", subscription.homepage);
            js.evaluate("clearSubscriptions()");
            js.evaluate("addSubscription(\"" + StringEscapeUtils.escapeJavaScript(jsonSub.toString()) + "\")");
        }
    }

    /**
     * Forces subscriptions refresh.
     */
    public void refreshSubscription() throws ScriptException {
        js.evaluate("refreshSubscriptions()");
    }

    /**
     * Selects which subscription to offer for the first time.
     * 
     * @return offered subscription
     */
    public Subscription offerSubscription() {
        Subscription selectedItem = null;
        String selectedPrefix = null;
        int matchCount = 0;
        for (Subscription subscription : getSubscriptions()) {
            if (selectedItem == null)
                selectedItem = subscription;

            String prefix = checkLocalePrefixMatch(subscription.prefixes);
            if (prefix != null) {
                if (selectedPrefix == null || selectedPrefix.length() < prefix.length()) {
                    selectedItem = subscription;
                    selectedPrefix = prefix;
                    matchCount = 1;
                } else if (selectedPrefix != null && selectedPrefix.length() == prefix.length()) {
                    matchCount++;

                    // If multiple items have a matching prefix of the
                    // same length select one of the items randomly,
                    // probability should be the same for all items.
                    // So we replace the previous match here with
                    // probability 1/N (N being the number of matches).
                    if (Math.random() * matchCount < 1) {
                        selectedItem = subscription;
                        selectedPrefix = prefix;
                    }
                }
            }
        }
        return selectedItem;
    }

    /**
     * Verifies that subscriptions are loaded and returns flag of subscription
     * presence.
     * 
     * @return true if at least one subscription is present and downloaded
     */
    public boolean verifySubscriptions() throws ScriptException {
        return (Boolean) js.evaluate("verifySubscriptions()");
    }

    /**
     * Checks if filters match request parameters.
     * 
     * @param url Request URL
     * @param query Request query string
     * @param reqHost Request host
     * @param refHost Request referrer header
     * @param accept Request accept header
     * @return true if matched filter was found
     */
    public Boolean matches(String url, String query, String reqHost, String refHost, String accept) throws Exception {
        return (Boolean) js.evaluate("matchesAny('" 
            + StringEscapeUtils.escapeJavaScript(url) + "', '" 
            + StringEscapeUtils.escapeJavaScript(query) + "', '" 
            + (reqHost != null ? StringEscapeUtils.escapeJavaScript(reqHost) : "") + "', '" 
            + (refHost != null ? StringEscapeUtils.escapeJavaScript(refHost) : "") + "', '" 
            + (accept != null ? StringEscapeUtils.escapeJavaScript(accept) : "") + "');");
    }

    /**
     * Notifies JS code that application entered interactive mode.
     */
    public void startInteractive() throws ScriptException {
        js.evaluate("startInteractive()");
        interactive = true;
    }

    /**
     * Notifies JS code that application quit interactive mode.
     */
    public void stopInteractive() throws ScriptException {
        js.evaluate("stopInteractive()");
        interactive = false;
    }

    /**
     * Returns prefixes that match current user locale.
     */
    public String checkLocalePrefixMatch(String[] prefixes) {
        if (prefixes == null || prefixes.length == 0)
            return null;

        String locale = Locale.getDefault().toString().toLowerCase();

        for (int i = 0; i < prefixes.length; i++)
            if (locale.startsWith(prefixes[i].toLowerCase()))
                return prefixes[i];

        return null;
    }
}
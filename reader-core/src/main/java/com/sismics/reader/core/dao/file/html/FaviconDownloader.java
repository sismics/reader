package com.sismics.reader.core.dao.file.html;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.ByteStreams;

/**
 * Utility to download a favicon from a website.
 *
 * @author jtremeaux 
 */
public class FaviconDownloader {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(FaviconDownloader.class);

    /**
     * Authorized favicon MIME types and corresponding file extensions.
     */
    final protected ImmutableMap<String, String> faviconMimeMap = new ImmutableMap.Builder<String, String>()
            .put("image/bmp", ".bmp")
            .put("image/gif", ".gif")
            .put("image/jpeg", ".jpg")
            .put("image/png", ".png")
            .put("image/x-icon", ".ico")
            .put("image/vnd.microsoft.icon", ".ico")
            .build();
    
    /**
     * Download the favicon from a feed's webpage.
     * Attempts to extract the favicon location from the page headers, or guess from common favicon locations.
     * 
     * @param pageUrl URL of an arbitrary page on the domain
     * @param directory Destination directory
     * @param fileName Destination filename (without extension)
     * @return Local file path or null if failed
     */
    public String downloadFaviconFromPage(String pageUrl, String directory, String fileName) {
        // Try to extract the favicon URL from the page specified in the feed
        String faviconUrl = null;
        try {
            FaviconExtractor extractor = new FaviconExtractor(pageUrl);
            extractor.readPage();
            faviconUrl = extractor.getFavicon();
        } catch (Exception e) {
            log.error("Error extracting icon from feed HTML page", e);
        }
        
        // Attempt to download a valid favicon from the HTML page 
        String localFilename = null;
        if (faviconUrl != null) {
            localFilename = downloadFavicon(faviconUrl, directory, fileName);
        }

        // Attempt to download a valid favicon from guessed URLs 
        final List<String> filenameList = ImmutableList.of(
                "favicon.png", "favicon.gif", "favicon.ico", "favicon.jpg", "favicon.jpeg", "favicon.bmp");
        Iterator<String> iterator = filenameList.iterator(); 
        while (localFilename == null && iterator.hasNext()) {
            String filename = iterator.next();
            faviconUrl = getFaviconUrl(pageUrl, filename);
            localFilename = downloadFavicon(faviconUrl, directory, fileName);
        }
        
        if (log.isInfoEnabled()) {
            if (localFilename != null) {
                log.info(MessageFormat.format("Favicon successfully downloaded to {0}", localFilename));
            } else {
                log.info(MessageFormat.format("Cannot find a valid favicon for feed {0} at page {1} or on the domain", fileName, pageUrl));
            }
        }
        return localFilename;
    }

    /**
     * Constructs a favicon URL from a feed path, and a guessed of the favicon file name (e.g. "favicon.ico").
     * 
     * @param pageUrl URL from a arbitrary page on the domain
     * @param fileName Favicon file name
     * @return Favicon URL
     */
    public String getFaviconUrl(String pageUrl, String fileName) {
        if (pageUrl != null) {
            try {
                URL url = new URL(pageUrl);
                return new URL(url.getProtocol(), url.getHost(), url.getPort(), "/" + fileName).toString();
            } catch (MalformedURLException e) {
                if (log.isErrorEnabled()) {
                    log.error(MessageFormat.format("Error building favicon URL from the page URL {0} with filename {1}", pageUrl, fileName));
                }
            }
        }
        return null;
    }

    /**
     * Attempts to download a favicon from an URL.
     * 
     * @param faviconUrl URL to download the favicon from
     * @param directory Destination directory
     * @param fileName Destination filename (without extension)
     * @return Local file path or null if failed
     */
    public String downloadFavicon(String faviconUrl, String directory, String fileName) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(faviconUrl).openConnection();
            String type = connection.getContentType();
    
            String extension = faviconMimeMap.get(type);
            if (extension != null) {
                File outputFile = new File(directory + File.separator + fileName + extension);
                ByteStreams.copy(connection.getInputStream(), new FileOutputStream(outputFile));
                return outputFile.getPath();
            }
        } catch (IOException e) {
            if (log.isErrorEnabled()) {
                log.error(MessageFormat.format(MessageFormat.format("Error downloading favicon at URL {0}", faviconUrl), e));
            }
        }
        return null;
    }

}

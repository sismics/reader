package com.sismics.reader.core.dao.file.html;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import com.sismics.reader.core.util.http.ReaderHttpClient;
import com.sismics.util.mime.MimeTypeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.List;

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
     * Authorized MIME types and corresponding file extensions.
     */
    final public ImmutableMap<String, String> FAVICON_MIME_TYPE_MAP = new ImmutableMap.Builder<String, String>()
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
            final FaviconExtractor extractor = new FaviconExtractor(pageUrl);
            new ReaderHttpClient() {
                
                @Override
                public Void process(InputStream is) throws Exception {
                    extractor.readPage(is);
                    return null;
                }
            }.open(new URL(pageUrl));
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
                "favicon.png", "favicon.gif", "favicon.ico", "favicon.jpg", "favicon.jpeg");
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
                log.info(MessageFormat.format("Cannot find a valid favicon for feed {0} at page {1} or at the domain root", fileName, pageUrl));
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
    public String downloadFavicon(final String faviconUrl, final String directory, final String fileName) {
        try {
            // Download the icon file to temporary location
            ReaderHttpClient<String> client = new ReaderHttpClient<String>() {

                @Override
                public String process(InputStream is) throws Exception {
                    File localFile = null;
                    try {
                        localFile = File.createTempFile("reader_favicon", ".ico");
                        if (ByteStreams.copy(is, new FileOutputStream(localFile)) > 0) {
                            // Check if it is a graphics file, we cannot rely on HTTP headers for Content-Type
                            String type = MimeTypeUtil.guessMimeType(localFile);
                            if (type != null) {
                                String extension = FAVICON_MIME_TYPE_MAP.get(type);
                                if (extension != null) {
                                    File outputFile = new File(directory + File.separator + fileName + extension);
                                    Files.copy(localFile, new FileOutputStream(outputFile));
                                    return outputFile.getPath();
                                }
                            }
                        }
                    } catch (FileNotFoundException e) {
                        if (log.isInfoEnabled()) {
                            log.info(MessageFormat.format("Favicon file not found at URL {0}", faviconUrl));
                        }
                    } finally {
                        // Clean up temporary local file
                        if (localFile != null) {
                            try {
                                localFile.delete();
                            } catch (Exception e) {
                                // NOP
                            }
                        }
                    }
                    return null;
                }
            };
            client.setTimeout(2000);
            return client.open(new URL(faviconUrl));
        } catch (Exception e) {
            if (log.isInfoEnabled()) {
                log.info(MessageFormat.format("Error downloading favicon at URL {0}", faviconUrl), e);
            }
        }
        return null;
    }
}

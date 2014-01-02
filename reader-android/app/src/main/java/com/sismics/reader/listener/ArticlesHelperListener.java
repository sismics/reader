package com.sismics.reader.listener;

/**
 * Listener on articles loading.

 * @author bgamard
 */
public interface ArticlesHelperListener {

    /**
     * Start loading articles.
     */
    public void onStart();

    /**
     * Articles successfully loaded.
     */
    public void onSuccess();

    /**
     * Error occurred while loading the articles.
     */
    public void onError();

    /**
     * End loading new articles.
     */
    public void onEnd();
}

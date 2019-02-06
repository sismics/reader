package com.sismics.reader.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.webkit.WebView;
import android.widget.ImageView;

import com.androidquery.AQuery;
import com.androidquery.callback.BitmapAjaxCallback;
import com.sismics.reader.R;
import com.sismics.reader.util.PreferenceUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;

/**
 * Fragment displaying an article.
 *
 * @author bgamard
 */
public class ArticleFragment extends Fragment {

    // Properties for the zoom in animation of enclosures
    private Animator currentAnimator;
    private int shortAnimationDuration;

    /**
     * True if the article has enclosure.
     */
    boolean hasEnclosure;

    /**
     * Create a new instance of ArticleFragment.
     */
    public static ArticleFragment newInstance(JSONObject json) {
        ArticleFragment f = new ArticleFragment();

        // Supply argument
        Bundle args = new Bundle();
        args.putString("json", json.toString());
        f.setArguments(args);

        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.article_fragment, container, false);
        final AQuery aq = new AQuery(view);

        // Configure WebView
        WebView webView = aq.id(R.id.articleWebView).getWebView();
        webView.getSettings().setUseWideViewPort(true);

        Bundle args = getArguments();
        if (args != null) {
            String jsonStr = args.getString("json");
            if (jsonStr != null) {
                try {
                    final JSONObject json = new JSONObject(jsonStr);

                    // Article font size from preference
                    String fontSize = PreferenceUtil.getStringPreference(getActivity(), PreferenceUtil.PREF_FONT_SIZE);

                    // HTML modification to fit the article content in the screen width
                    String html = json.optString("description");
                    try {
                        html = URLEncoder.encode("<!DOCTYPE html>" +
                                "<html>" +
                                "<head>" +
                                "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />" +
                                "<meta name=\"viewport\" content=\"initial-scale=1, minimum-scale=1, width=device-width, maximum-scale=1, user-scalable=no\" />" +
                                "<style>" +
                                "img { max-width: 100%; height: auto; display: block; margin: 8px; }\n" +
                                "iframe { display: none; }\n" +
                                "pre { max-width: 100%; overflow: hidden; }\n" +
                                "a {" +
                                "  text-decoration: none;" +
                                "  color: #0099cc;" +
                                "}\n" +
                                "body {" +
                                "  color: #191919;" +
                                "  font-size: " + fontSize + "pt;" +
                                "  font-family: 'sans-serif-light', 'sans-serif';" +
                                "  line-height: 150%; }" +
                                "</style>" +
                                "</head>" +
                                "<body>" +
                                html +
                                "</body>" +
                                "</html>", "UTF-8").replaceAll("\\+", "%20");
                    } catch (UnsupportedEncodingException e) {
                        Log.e("ArticleFragment", "Error modifying article HTML", e);
                    }
                    webView.loadData(html, "text/html; charset=UTF-8", null);

                    // Title
                    aq.id(R.id.title)
                            .text(Html.fromHtml(json.optString("title")))
                            .clicked(new OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(json.optString("url")));
                                    startActivity(intent);
                                }
                            });

                    // Subscription
                    JSONObject subscription = json.optJSONObject("subscription");
                    String creator = json.optString("creator");
                    aq.id(R.id.author).text(subscription.optString("title"));

                    // Date
                    aq.id(R.id.date).text((!creator.isEmpty() ? creator + " " : "")
                            + DateUtils.getRelativeTimeSpanString(json.optLong("date"), new Date().getTime(), 0).toString());
                    String faviconUrl = PreferenceUtil.getServerUrl(getActivity()) + "/api/subscription/" + subscription.optString("id") + "/favicon";

                    // Favicon
                    aq.id(R.id.imgFavicon).image(new BitmapAjaxCallback()
                            .url(faviconUrl)
                            .fallback(R.drawable.ic_launcher)
                            .animation(AQuery.FADE_IN_NETWORK)
                            .cookie("auth_token", PreferenceUtil.getAuthToken(getActivity())));

                    // Enclosure
                    JSONObject enclosure = json.optJSONObject("enclosure");
                    hasEnclosure = enclosure != null;
                    if (enclosure != null) {
                        String type = enclosure.optString("type");
                        String url = enclosure.optString("url");
                        if (url != null && type != null
                                && (type.equals("image/jpeg") || type.equals("image/jpg") || type.equals("image/png") || type.equals("image/gif"))) {
                            aq.id(R.id.enclosure).visible();
                            aq.id(R.id.imgEnclosure)
                                    .progress(R.id.progressEnclosure)
                                    .image(url, true, true, 800, AQuery.GONE, null, AQuery.FADE_IN_NETWORK)
                                    .clicked(new OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            zoomImageFromThumb(aq.id(R.id.imgEnclosure).getImageView());
                                        }
                                    });
                        }
                    }
                } catch (JSONException e) {
                    Log.e("ArticleFragment", "Unable to parse JSON", e);
                }
            }
        }

        // Retrieve and cache the system's default "short" animation time.
        shortAnimationDuration = getResources().getInteger(android.R.integer.config_shortAnimTime);

        return view;
    }

    /**
     * Zoom the enclosure image from its thumbnail.
     *
     * @param thumbView View containing the thumbnail
     */
    private void zoomImageFromThumb(final ImageView thumbView) {
        if (thumbView.getDrawable() == null) {
            // The image is not yet loaded, can't zoom
            return;
        }

        // Hide the progess bar
        getView().findViewById(R.id.progressEnclosure).setVisibility(View.GONE);

        // If there's an animation in progress, cancel it
        // immediately and proceed with this one.
        if (currentAnimator != null) {
            currentAnimator.cancel();
        }

        // Load the high-resolution "zoomed-in" image.
        final ImageView expandedImageView = (ImageView) getView().findViewById(R.id.expanded_image);
        expandedImageView.setImageBitmap(((BitmapDrawable)thumbView.getDrawable()).getBitmap());

        // Calculate the starting and ending bounds for the zoomed-in image.
        // This step involves lots of math. Yay, math.
        final Rect startBounds = new Rect();
        final Rect finalBounds = new Rect();
        final Point globalOffset = new Point();

        // The start bounds are the global visible rectangle of the thumbnail,
        // and the final bounds are the global visible rectangle of the container
        // view. Also set the container view's offset as the origin for the
        // bounds, since that's the origin for the positioning animation
        // properties (X, Y).
        thumbView.getGlobalVisibleRect(startBounds);
        getView().findViewById(R.id.container)
                .getGlobalVisibleRect(finalBounds, globalOffset);
        startBounds.offset(-globalOffset.x, -globalOffset.y);
        finalBounds.offset(-globalOffset.x, -globalOffset.y);

        // Adjust the start bounds to be the same aspect ratio as the final
        // bounds using the "center crop" technique. This prevents undesirable
        // stretching during the animation. Also calculate the start scaling
        // factor (the end scaling factor is always 1.0).
        float startScale;
        if ((float) finalBounds.width() / finalBounds.height()
                > (float) startBounds.width() / startBounds.height()) {
            // Extend start bounds horizontally
            startScale = (float) startBounds.height() / finalBounds.height();
            float startWidth = startScale * finalBounds.width();
            float deltaWidth = (startWidth - startBounds.width()) / 2;
            startBounds.left -= deltaWidth;
            startBounds.right += deltaWidth;
        } else {
            // Extend start bounds vertically
            startScale = (float) startBounds.width() / finalBounds.width();
            float startHeight = startScale * finalBounds.height();
            float deltaHeight = (startHeight - startBounds.height()) / 2;
            startBounds.top -= deltaHeight;
            startBounds.bottom += deltaHeight;
        }

        // Hide the thumbnail and show the zoomed-in view. When the animation
        // begins, it will position the zoomed-in view in the place of the
        // thumbnail.
        thumbView.setAlpha(0f);
        expandedImageView.setVisibility(View.VISIBLE);

        // Set the pivot point for SCALE_X and SCALE_Y transformations
        // to the top-left corner of the zoomed-in view (the default
        // is the center of the view).
        expandedImageView.setPivotX(0f);
        expandedImageView.setPivotY(0f);

        // Construct and run the parallel animation of the four translation and
        // scale properties (X, Y, SCALE_X, and SCALE_Y).
        AnimatorSet set = new AnimatorSet();
        set
                .play(ObjectAnimator.ofFloat(expandedImageView, View.X,
                        startBounds.left, finalBounds.left))
                .with(ObjectAnimator.ofFloat(expandedImageView, View.Y,
                        startBounds.top, finalBounds.top))
                .with(ObjectAnimator.ofFloat(expandedImageView, View.SCALE_X,
                        startScale, 1f)).with(ObjectAnimator.ofFloat(expandedImageView,
                View.SCALE_Y, startScale, 1f));
        set.setDuration(shortAnimationDuration);
        set.setInterpolator(new DecelerateInterpolator());
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                currentAnimator = null;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                currentAnimator = null;
            }
        });
        set.start();
        currentAnimator = set;

        // Upon clicking the zoomed-in image, it should zoom back down
        // to the original bounds and show the thumbnail instead of
        // the expanded image.
        final float startScaleFinal = startScale;
        expandedImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentAnimator != null) {
                    currentAnimator.cancel();
                }

                // Animate the four positioning/sizing properties in parallel,
                // back to their original values.
                AnimatorSet set = new AnimatorSet();
                set.play(ObjectAnimator
                        .ofFloat(expandedImageView, View.X, startBounds.left))
                        .with(ObjectAnimator
                                .ofFloat(expandedImageView,
                                        View.Y,startBounds.top))
                        .with(ObjectAnimator
                                .ofFloat(expandedImageView,
                                        View.SCALE_X, startScaleFinal))
                        .with(ObjectAnimator
                                .ofFloat(expandedImageView,
                                        View.SCALE_Y, startScaleFinal));
                set.setDuration(shortAnimationDuration);
                set.setInterpolator(new DecelerateInterpolator());
                set.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        thumbView.setAlpha(1f);
                        expandedImageView.setVisibility(View.GONE);
                        currentAnimator = null;
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        thumbView.setAlpha(1f);
                        expandedImageView.setVisibility(View.GONE);
                        getActivity().getActionBar().show();
                        currentAnimator = null;
                    }
                });
                set.start();
                currentAnimator = set;
            }
        });
    }
}

package com.sismics.reader.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.sismics.reader.R;
import com.sismics.reader.resource.SubscriptionResource;

import org.apache.http.Header;
import org.json.JSONObject;

/**
 * Add subscription dialog fragment.
 *
 * @author bgamard.
 */
public class AddSubscriptionDialogFragment extends DialogFragment {

    /**
     * New subscription listener.
     */
    AddSubscriptionDialogListener addSubscriptionDialogListener;

    /**
     * Listener called when a subscription is added.
     */
    public interface AddSubscriptionDialogListener {
        public void onSubscriptionAdded(JSONObject json);
    }

    /**
     * Setter of addSubscriptionDialogListener.
     *
     * @param addSubscriptionDialogListener The listener
     */
    public void setAddSubscriptionDialogListener(AddSubscriptionDialogListener addSubscriptionDialogListener) {
        this.addSubscriptionDialogListener = addSubscriptionDialogListener;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Inflate the dialog view
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.add_subscription_dialog_fragment, null);

        // Create the dialog without button click listeners
        builder.setTitle(R.string.add_subscription)
                .setView(view)
                .setPositiveButton(R.string.add, null)
                .setNegativeButton(R.string.cancel, null);

        // Create the AlertDialog object and return it
        return builder.create();
    }

    @Override
    public void show(FragmentManager manager, String tag) {
        super.show(manager, tag);
        getFragmentManager().executePendingTransactions();
        final AlertDialog dialog = (AlertDialog) getDialog();

        // Button click listeners are added now to control the dismiss behavior
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                // Find view component
                final EditText txtAddSubscription = (EditText) dialog.findViewById(R.id.txtAddSubscription);
                final View progressBar = dialog.findViewById(R.id.progressBar);

                // Validate the URL
                String url = txtAddSubscription.getText().toString();
                if (url.trim().length() == 0) {
                    return;
                }

                // Add the subscription
                txtAddSubscription.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
                SubscriptionResource.add(getActivity(), url, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(JSONObject json) {
                        if (addSubscriptionDialogListener != null) {
                            // Call the listener
                            addSubscriptionDialogListener.onSubscriptionAdded(json);
                        }

                        // Feedback that the subscription was added
                        Toast.makeText(getActivity(), R.string.add_subscription_success, Toast.LENGTH_LONG).show();

                        // Close the dialog
                        dismiss();
                    }

                    @Override
                    public void onFailure(final int statusCode, final Header[] headers, final byte[] responseBytes, final Throwable throwable) {
                        // Feedback that the subscription was not added
                        txtAddSubscription.setError(getText(R.string.add_subscription_error));
                    }

                    @Override
                    public void onFinish() {
                        txtAddSubscription.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.GONE);
                    }
                });
            }
        });
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);

        // Cancel pending requests to subscription API
        SubscriptionResource.cancel(getActivity());
    }
}

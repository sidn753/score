package com.amusementlabs.whatsthescore;

import android.content.Intent;
import android.net.Uri;
import android.widget.TextView;
import android.widget.Toast;
import com.actionbarsherlock.app.SherlockFragment;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

//import org.holoeverywhere.LayoutInflater;
//import org.holoeverywhere.app.Fragment;

@EFragment(R.layout.frag_about)
public class FragAbout extends SherlockFragment {

    @ViewById(R.id.about_feedback_link)
    TextView mEmailLink;


    @ViewById(R.id.about_review_link)
    TextView mReviewLink;

    @ViewById(R.id.about_share_link)
    TextView mShareLink;


    @Click(R.id.about_feedback_link)
    void sendEmail() {

        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("message/rfc822");
        i.putExtra(Intent.EXTRA_EMAIL, new String[]{getString(R.string.about_text_email_addr)});
        i.putExtra(Intent.EXTRA_SUBJECT, "What's the Score feedback");
        try {
            startActivity(Intent.createChooser(i, "Send mail..."));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(getActivity(), "There are no email clients installed.", Toast.LENGTH_SHORT).show();
        }
    }

    @Click(R.id.about_review_link)
    void openReviewLink() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("market://details?id=com.amusementlab.whatsthescore"));
        startActivity(intent);
    }

    @Click(R.id.about_share_link)
    void openShareIntent() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setData(Uri.parse("market://details?id=com.amusementlab.whatsthescore"));

        startActivity(Intent.createChooser(intent, getString(R.string.frag_about_share_text)));
    }


}

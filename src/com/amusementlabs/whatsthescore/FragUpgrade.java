package com.amusementlabs.whatsthescore;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import android.content.Intent;
import android.net.Uri;
import android.widget.Button;
import com.actionbarsherlock.app.SherlockFragment;

//import org.holoeverywhere.LayoutInflater;
//import org.holoeverywhere.app.Fragment;

@EFragment(R.layout.frag_upgrade)
public class FragUpgrade extends SherlockFragment {

    @ViewById(R.id.button_upgrade)
    Button mUpgradeButton;


    @Click(R.id.button_upgrade)
    void openMarketLink() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(getString(R.string.upgrade_market_uri)));
        startActivity(intent);
    }


}

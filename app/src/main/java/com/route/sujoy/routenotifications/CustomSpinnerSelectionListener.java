package com.route.sujoy.routenotifications;

import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Toast;

class CustomSpinnerSelectionListener implements OnItemSelectedListener {

    public void onItemSelected(AdapterView<?> parent, View view, int pos,
                               long id) {

        Toast.makeText(parent.getContext(),
                parent.getItemAtPosition(pos).toString(),
                Toast.LENGTH_LONG).show();
        RoutesUtils.routeName = parent.getItemAtPosition(pos).toString();

    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
        // TODO Auto-generated method stub

    }

}
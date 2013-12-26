/*************************************************************************************************
 * Android QB Rating App
 * Jason Barkes - http://jbarkes.blogspot.com
 * 11/13/2013
 *************************************************************************************************/
package barkes.football.qbcalc;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Fragment to display the App's EULA dialog.
 */

public class EulaDialogFrag extends DialogFragment {

    /**
     * Create a new instance of AboutDialogFrag. This method is here in case we need to pass any
     * arguments in the future.
     */
    static EulaDialogFrag newInstance() {
        EulaDialogFrag frag = new EulaDialogFrag();
        return frag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, android.R.style.Theme_Holo_Light_Dialog);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_eula, container, false);
        return view;
    }
}
package bledoor.river.se.bledoor;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

/**
 * this class is defining functionality of a password widget
 */
public class PasswordDialog extends DialogFragment {

    final String LOGTAG = "PasswordDialog";
    PasswordDialogInterface passwordDialogInterface;
    private EditText passwordText;

    //declater interface
    public interface PasswordDialogInterface{
        public void password(String password);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try{
            passwordDialogInterface = (PasswordDialogInterface)activity;
        }catch (ClassCastException ex){
            Log.e(LOGTAG, "Client Activity to the Password dialog has to use interface");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.password_dialog_fragment, null);
        passwordText = (EditText)view.findViewById(R.id.password_dialog_password_text);

        builder.setView(view);

        builder.setIcon(R.drawable.padlock);
        builder.setTitle(R.string.password_dialog_fragment_title);

        //positive button
        builder.setPositiveButton(R.string.password_dialog_fragment_ok_text, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d(LOGTAG,"setPositiveButton onClick");
                //get password from fields
                String password = passwordText.getText().toString();
                // and send it back to dialog owner
                passwordDialogInterface.password(password);
            }
        });

        //positive button
        builder.setNegativeButton(R.string.password_dialog_fragment_cancel_text, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d(LOGTAG, "setNegativeButton onClick");
                //don't do anything
            }
        });

        return builder.create();
    }



    }

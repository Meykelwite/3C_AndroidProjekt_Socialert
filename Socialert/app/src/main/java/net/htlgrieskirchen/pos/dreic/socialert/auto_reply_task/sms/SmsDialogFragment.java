package net.htlgrieskirchen.pos.dreic.socialert.auto_reply_task.sms;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import net.htlgrieskirchen.pos.dreic.socialert.R;
import net.htlgrieskirchen.pos.dreic.socialert.Variables;
import net.htlgrieskirchen.pos.dreic.socialert.auto_reply_task.TaskListener;
import net.htlgrieskirchen.pos.dreic.socialert.auto_reply_task.TaskMasterFragment;

import java.util.HashMap;
import java.util.Map;

// https://medium.com/alexander-schaefer/implementing-the-new-material-design-full-screen-dialog-for-android-e9dcc712cb38
public class SmsDialogFragment extends DialogFragment {
    public static final String TAG = "sms_dialog";
    private static final int RQ_PICK_CONTACT = 69542;
    private static final int RQ_ACCESS_FINE_LOCATION = 123456;
    private MaterialToolbar toolbar;
    private TextInputLayout textField_addContact;
    private TextInputEditText et_addContact;
    private TextInputEditText et_message;
    private ChipGroup chipGroup;
    private TextWatcher textChangedListener;
    private Button btn_location;
    private Button btn_address;
    private Switch switch_all_contacts;

    // for SmsTask object
    private HashMap<String, String> receiver = new HashMap<>();

    private SmsTask task;
    private int position;
    private boolean edit;


    private boolean isAddNumberIcon = false; // true, wenn et_addContact nicht leer

    // Referenz auf die Activity mithilfe eines Objekts vom Typ OnAddTaskListener
    private TaskListener listener;

    private boolean isGPSAllowed = false;


    public static SmsDialogFragment display(FragmentManager fragmentManager) {
        SmsDialogFragment smsDialogFragment = new SmsDialogFragment();
        smsDialogFragment.show(fragmentManager, TAG);
        return smsDialogFragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        // Activity als Listener für die "Klick-Events" registieren. Allerdings sollten wir prüfen, ob die Activity auch tatsächlich das Interface implementiert.
        if (context instanceof TaskMasterFragment.OnSelectionChangedListener) {
            listener = (TaskListener) context;
        } else {
            Toast.makeText(getContext(), "onAttach: Activity does not implement OnAddTaskListener", Toast.LENGTH_SHORT).show();
        }

    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.Theme_Socialert_FullScreenDialog);
        if (getArguments() != null) {
            task = (SmsTask) getArguments().getSerializable("task");
            position = getArguments().getInt("position");
            edit = true;
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_sms_task_auto_reply_dialog, container, false);

        toolbar = view.findViewById(R.id.toolbar);
        textField_addContact = view.findViewById(R.id.textField_addContact);
        et_addContact = view.findViewById(R.id.et_addContact);
        et_message = view.findViewById(R.id.et_message);
        chipGroup = view.findViewById(R.id.chipGroup);
        switch_all_contacts = view.findViewById(R.id.switch_all_contacts);
        initTextChangedListener();
        addTextFieldListeners();

        if (edit) {
            et_message.setText(task.getMessage());
            Map<String, String> receiver = task.getReceiver();
            if (!receiver.isEmpty()) {
                textField_addContact.setVisibility(View.VISIBLE);
                for (Map.Entry<String, String> entry : receiver.entrySet()) {
                    if (entry.getValue().isEmpty()) { // no name, just phoneNumber
                        createChip(entry.getKey());
                    } else {
                        createChip(entry.getKey(), entry.getValue());
                    }
                    addReceiver(entry.getKey(), entry.getValue());
                }
            }

        }

        btn_address = view.findViewById(R.id.btn_address);
        btn_location = view.findViewById(R.id.btn_location);

        btn_address.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermissionGPS();
                if (isGPSAllowed) {
                    et_message.getText().insert(et_message.getSelectionStart(), Variables.ADDRESS);
                }
            }
        });

        btn_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermissionGPS();
                if (isGPSAllowed) {
                    et_message.getText().insert(et_message.getSelectionStart(), Variables.LOCATION);
                }
            }
        });

        switch_all_contacts.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    textField_addContact.setVisibility(View.GONE);
                    chipGroup.removeAllViews();
                    receiver.clear();
                } else {
                    textField_addContact.setVisibility(View.VISIBLE);
                }
            }
        });

        return view;
    }

    private void checkPermissionGPS() {
        String permission = Manifest.permission.ACCESS_FINE_LOCATION;
        if (getActivity().checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
            getActivity().requestPermissions(new String[]{permission}, RQ_ACCESS_FINE_LOCATION);
        } else {
            isGPSAllowed = true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case RQ_ACCESS_FINE_LOCATION:
                if (grantResults.length > 0 &&
                        grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getContext(), "Permission ACCESS_FINE_LOCATION denied!", Toast.LENGTH_SHORT).show();
                } else {
                    isGPSAllowed = true;
                }
                break;

        }

    }

    private void initTextChangedListener() {
        textChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!isAddNumberIcon && s.length() > 0) {
                    textField_addContact.setEndIconDrawable(R.drawable.ic_baseline_person_add_24);
                    textField_addContact.setEndIconContentDescription(R.string.content_description_end_icon_add_number);
                    isAddNumberIcon = true;
                } else if (isAddNumberIcon && s.length() == 0) {
                    textField_addContact.setEndIconDrawable(R.drawable.ic_baseline_person_24);
                    textField_addContact.setEndIconContentDescription(R.string.content_description_end_icon);
                    isAddNumberIcon = false;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };
    }

    private void addTextFieldListeners() {
        textField_addContact.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isAddNumberIcon) { // TextField is not empty!
                    String phoneNumber = et_addContact.getText().toString();
                    String regex = "[^a-zA-Z]{3,}";
                    if (phoneNumber.matches(regex)) {
                        createChip(phoneNumber);
                        addReceiver(phoneNumber, "");
                        et_addContact.getText().clear();
                    } else {
                        Toast.makeText(getContext(), "Ungültige Telefonnummer!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Intent pickContact = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                    pickContact.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
                    startActivityForResult(pickContact, RQ_PICK_CONTACT);
                }
            }
        });
        et_addContact.removeTextChangedListener(textChangedListener);
        et_addContact.addTextChangedListener(textChangedListener);


    }

    private void addReceiver(String phoneNumber, String name) {
        receiver.clear();
        receiver.put(phoneNumber, name);
    }

    // https://stackoverflow.com/questions/33954358/how-to-select-contact-number-from-contact-list-using-android-studio
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case RQ_PICK_CONTACT:
                if (data != null) {
                    Uri contactData = data.getData();
                    String phoneNumber = getPhoneNumber(contactData);
                    String name = getContactName(contactData);
                    createChip(phoneNumber, name);
                    addReceiver(phoneNumber, name);
                }
                break;
        }

    }

    private void createChip(String phoneNumber, String name) {
        chipGroup.removeAllViews();
        Chip chip = new Chip(getContext());
        chip.setText(name + " (" + phoneNumber + ")");
        chip.setChipIconResource(R.drawable.ic_baseline_person_24);
        chip.setCloseIconVisible(true);
        chip.setOnCloseIconClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chipGroup.removeView(chip);
                receiver.remove(phoneNumber);
            }
        });

        chipGroup.addView(chip);
    }

    private void createChip(String phoneNumber) {
        chipGroup.removeAllViews();
        Chip chip = new Chip(getContext());
        chip.setText(phoneNumber);
        chip.setChipIconResource(R.drawable.ic_baseline_person_24);
        chip.setCloseIconVisible(true);
        chip.setOnCloseIconClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chipGroup.removeView(chip);
                receiver.remove(phoneNumber);
            }
        });
        chipGroup.addView(chip);
    }

    private String getPhoneNumber(Uri contactData) {
        Cursor c = getContext().getContentResolver().query(contactData, null, null, null, null);
        if (c.moveToFirst()) {
            int phoneIndex = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
            String num = c.getString(phoneIndex);
            return num;
        }
        return "";
    }

    private String getContactName(Uri contactData) {
        Cursor c = getContext().getContentResolver().query(contactData, null, null, null, null);
        if (c.moveToFirst()) {
            int nameIndex = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
            String name = c.getString(nameIndex);
            return name;
        }
        return "";
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        toolbar.setNavigationOnClickListener(v -> dismiss());
        toolbar.setTitle("SMS");
        toolbar.inflateMenu(R.menu.fragment_dialog);

        // den erstellten Task an die Activity senden
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() { // save Action
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (et_message.getText().length() > 160) {
                    Toast.makeText(getContext(), "Die Nachricht ist zu lang!", Toast.LENGTH_SHORT).show();
                } else if (et_message.getText().toString().isEmpty()) {
                    Toast.makeText(getContext(), "Sie müssen eine Nachricht eintragen!", Toast.LENGTH_SHORT).show();
                } else {
                    SmsTask newTask = new SmsTask(et_message.getText().toString(), receiver);
                    if (edit) {
                        newTask.setCompleted(task.isCompleted());
                        if (task.equals(newTask)) {
                            Toast.makeText(getContext(), "Sie haben keine Änderungen vorgenommen!", Toast.LENGTH_SHORT).show();
                        } else {
                            dismiss();
                            listener.onEditTask(position, newTask);
                        }
                    } else {
                        listener.onAddTask(newTask);
                        dismiss();
                    }
                }
                return true;
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setLayout(width, height);
            dialog.getWindow().setWindowAnimations(R.style.Theme_Socialert_DialogSlide);
        }
    }
}
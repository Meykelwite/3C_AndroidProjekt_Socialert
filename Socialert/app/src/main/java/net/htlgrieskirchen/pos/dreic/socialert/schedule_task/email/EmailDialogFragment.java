package net.htlgrieskirchen.pos.dreic.socialert.schedule_task.email;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.gmail.GmailScopes;

import net.htlgrieskirchen.pos.dreic.socialert.R;
import net.htlgrieskirchen.pos.dreic.socialert.Variables;
import net.htlgrieskirchen.pos.dreic.socialert.schedule_task.ScheduleTask;
import net.htlgrieskirchen.pos.dreic.socialert.schedule_task.ScheduleTaskActivity;
import net.htlgrieskirchen.pos.dreic.socialert.schedule_task.TaskListener;
import net.htlgrieskirchen.pos.dreic.socialert.schedule_task.TaskMasterFragment;
import net.htlgrieskirchen.pos.dreic.socialert.schedule_task.sms.SmsTask;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

// https://medium.com/alexander-schaefer/implementing-the-new-material-design-full-screen-dialog-for-android-e9dcc712cb38
public class EmailDialogFragment extends DialogFragment {
    public static final String TAG = "email_dialog";
    private static final int RQ_PICK_CONTACT = 7489;
    private static final int RQ_ACCESS_FINE_LOCATION = 1234;
    private MaterialToolbar toolbar;
    private TextInputLayout textField_addContact;
    private TextInputEditText et_addContact;
    private TextInputEditText et_message;
    private TextInputEditText et_subject;
    private ChipGroup chipGroup;
    private TextWatcher textChangedListener;
    private DatePicker datePicker;
    private TimePicker timePicker;
    private Button btn_location;
    private Button btn_address;
    private TextView tv_account;

    // for EmailTask object
    private HashMap<String, String> receivers = new HashMap<>();

    private EmailTask task;
    private int position;
    private boolean edit;

    private boolean isAddEmailIcon = false; // true, wenn et_addContact nicht leer

    // Referenz auf die Activity mithilfe eines Objekts vom Typ OnAddTaskListener
    private TaskListener listener;

    private String accountName;

    private boolean isGPSAllowed = false;


    public static EmailDialogFragment display(FragmentManager fragmentManager) {
        EmailDialogFragment emailDialogFragment = new EmailDialogFragment();
        emailDialogFragment.show(fragmentManager, TAG);
        return emailDialogFragment;
    }

//    // https://medium.com/@royanimesh2211/android-dialogfragment-to-activity-communication-fb652112850e
//    public interface OnAddTaskListener {
//        void onAddTask(ScheduleTask task);
//    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        // Activity als Listener f??r die "Klick-Events" registieren. Allerdings sollten wir pr??fen, ob die Activity auch tats??chlich das Interface implementiert.
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
            task = (EmailTask) getArguments().getSerializable("task");
            position = getArguments().getInt("position");
            edit = true;
            accountName = task.getAccountName();
        } else {
            accountName = ((ScheduleTaskActivity) getActivity()).getSelectedAccountName();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_email_task_dialog, container, false);

        toolbar = view.findViewById(R.id.toolbar);
        textField_addContact = view.findViewById(R.id.textField_addContact);
        et_addContact = view.findViewById(R.id.et_addContact);
        et_message = view.findViewById(R.id.et_message);
        et_subject = view.findViewById(R.id.et_subject);
        chipGroup = view.findViewById(R.id.chipGroup);
        timePicker = view.findViewById(R.id.timePicker);
        timePicker.setIs24HourView(true);
        datePicker = view.findViewById(R.id.datePicker);
        tv_account = view.findViewById(R.id.tv_account);
        if (accountName != null && !edit) {
            tv_account.append(accountName);
        }
        initTextChangedListener();
        addTextFieldListeners();

        if (edit) {
            et_message.setText(task.getMessage());
            tv_account.append(task.getAccountName());
            et_subject.setText(task.getSubject());
            LocalDateTime localDateTime = task.getTimeAsLocalDateTime();
            datePicker.init(localDateTime.getYear(), localDateTime.getMonthValue() - 1, localDateTime.getDayOfMonth(), DatePicker::updateDate);
            timePicker.setHour(localDateTime.getHour());
            timePicker.setMinute(localDateTime.getMinute());

            Map<String, String> receivers = task.getReceivers();
            for (Map.Entry<String, String> entry : receivers.entrySet()) {
                if (entry.getValue().isEmpty()) { // no name, just email
                    createChip(entry.getKey());

                } else {
                    createChip(entry.getKey(), entry.getValue());
                }
                addReceiver(entry.getKey(), entry.getValue());
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
                if (!isAddEmailIcon && s.length() > 0) {
                    textField_addContact.setEndIconDrawable(R.drawable.ic_baseline_person_add_24);
                    textField_addContact.setEndIconContentDescription(R.string.content_description_end_icon_add_number);
                    isAddEmailIcon = true;
                } else if (isAddEmailIcon && s.length() == 0) {
                    textField_addContact.setEndIconDrawable(R.drawable.ic_baseline_person_24);
                    textField_addContact.setEndIconContentDescription(R.string.content_description_end_icon);
                    isAddEmailIcon = false;
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
                if (isAddEmailIcon) { // TextField is not empty!
                    String email = et_addContact.getText().toString();
                    String regex = "[A-Za-z0-9!#$%&'\\*\\+\\-\\/=?^_`{|}~]([A-Za-z0-9\\.!#$%&'\\*\\+\\-\\/=?^_`{|}~]+)?[A-Za-z0-9!#$%&'\\*\\+\\-\\/=?^_`{|}~]@[A-Za-z0-9\\.!#$%&'\\*\\+\\-\\/=?^_`{|}~]{2,}\\.[A-Za-z]{2,6}";
                    if (email.matches(regex)) {
                        createChip(email);
                        addReceiver(email, "");
                        et_addContact.getText().clear();
                    } else {
                        Toast.makeText(getContext(), "Ung??ltige E-Mail Adresse!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Intent pickContact = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                    pickContact.setType(ContactsContract.CommonDataKinds.Email.CONTENT_TYPE);
                    startActivityForResult(pickContact, RQ_PICK_CONTACT);
                }
            }
        });
        et_addContact.removeTextChangedListener(textChangedListener);
        et_addContact.addTextChangedListener(textChangedListener);
    }

    private void addReceiver(String email, String name) {
        if (receivers.containsKey(email)) {
            Toast.makeText(getContext(), "Empf??nger bereits vorhanden!", Toast.LENGTH_SHORT).show();
        } else {
            receivers.put(email, name);
        }
    }

    // https://stackoverflow.com/questions/33954358/how-to-select-contact-number-from-contact-list-using-android-studio
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case RQ_PICK_CONTACT:
                if (data != null) {
                    Uri contactData = data.getData();
                    String email = getEmail(contactData);
                    String name = getContactName(contactData);
                    createChip(email, name);
                    addReceiver(email, name);
                }
                break;
        }

    }

    private void createChip(String email, String name) {
        Chip chip = new Chip(getContext());
        chip.setText(name + " (" + email + ")");
        chip.setChipIconResource(R.drawable.ic_baseline_person_24);
        chip.setCloseIconVisible(true);
        chip.setOnCloseIconClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chipGroup.removeView(chip);
                receivers.remove(email);
            }
        });

        chipGroup.addView(chip);
    }

    private void createChip(String email) {
        Chip chip = new Chip(getContext());
        chip.setText(email);
        chip.setChipIconResource(R.drawable.ic_baseline_person_24);
        chip.setCloseIconVisible(true);
        chip.setOnCloseIconClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chipGroup.removeView(chip);
                receivers.remove(email);
            }
        });
        chipGroup.addView(chip);
    }

    private String getEmail(Uri contactData) {
        Cursor c = getContext().getContentResolver().query(contactData, null, null, null, null);
        if (c.moveToFirst()) {
            int emailIndex = c.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA);
            String email = c.getString(emailIndex);
            return email;
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
        toolbar.setTitle("Email");
        toolbar.inflateMenu(R.menu.fragment_dialog);

        // den erstellten Task an die Activity senden
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() { // save Action
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (receivers.isEmpty() && et_message.getText().toString().isEmpty()) {
                    Toast.makeText(getContext(), "Bitte Empf??nger und Nachrichteninhalt hinzuf??gen!", Toast.LENGTH_SHORT).show();
                } else if (receivers.isEmpty()) {
                    Toast.makeText(getContext(), "Bitte Empf??nger eintragen!", Toast.LENGTH_SHORT).show();
                } else if (et_message.getText().toString().isEmpty()) {
                    Toast.makeText(getContext(), "Sie m??ssen eine Nachricht eintragen!", Toast.LENGTH_SHORT).show();
                } else if (et_subject.getText().toString().isEmpty()) {
                    Toast.makeText(getContext(), "Sie m??ssen einen Betreff eingeben!", Toast.LENGTH_SHORT).show();
                } else {
                    LocalDateTime dateTime = LocalDateTime.of(datePicker.getYear(), datePicker.getMonth() + 1, datePicker.getDayOfMonth(), timePicker.getHour(), timePicker.getMinute());
                    EmailTask newTask = new EmailTask(et_message.getText().toString(), dateTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")), receivers, et_subject.getText().toString(), accountName);
                    if (edit) {
                        newTask.setCompleted(task.isCompleted());
                        if (task.equals(newTask)) {
                            Toast.makeText(getContext(), "Sie haben keine ??nderungen vorgenommen!", Toast.LENGTH_SHORT).show();
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
package net.htlgrieskirchen.pos.dreic.socialert.schedule_task.email;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.googleapis.util.Utils;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.Base64;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.Message;

import net.htlgrieskirchen.pos.dreic.socialert.R;
import net.htlgrieskirchen.pos.dreic.socialert.schedule_task.ScheduleTask;
import net.htlgrieskirchen.pos.dreic.socialert.schedule_task.ScheduleTaskActivity;
import net.htlgrieskirchen.pos.dreic.socialert.schedule_task.ScheduleTaskManager;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;
import java.util.Set;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import static androidx.core.app.ActivityCompat.startActivityForResult;

// Async Task for sending Mail using GMail OAuth
public class SendEmailTask extends AsyncTask<Void, Void, String> {

    private GoogleAccountCredential mCredential;
    private Gmail mService;
    private EmailTask task;
    private ScheduleTaskManager taskManager;

    public SendEmailTask(Context context, EmailTask task, ScheduleTaskManager taskManager) {
        GoogleAccountCredential credential =

                GoogleAccountCredential.usingOAuth2(
                        context, Arrays.asList(new String[]{GmailScopes.GMAIL_SEND}))
                        .setBackOff(new ExponentialBackOff());

        credential.setSelectedAccountName(task.getAccountName());

        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        mService = new Gmail.Builder(
                transport, jsonFactory, credential)
                .build();

        this.task = task;
        this.mCredential = credential;
        this.taskManager = taskManager;
    }

    @Override
    protected String doInBackground(Void... params) {
        try {
            return getDataFromApi();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String getDataFromApi() throws IOException {
        // getting Values for to Address, from Address, Subject and Body
        String user = mCredential.getSelectedAccountName();
        Set<String> to = task.getReceivers().keySet();
        String from = mCredential.getSelectedAccountName();
        String subject = task.getSubject();
        String body = task.getMessage();
        String response = "";
        MimeMessage mimeMessage = null;
        try {
            mimeMessage = createEmail(to, from, subject, body);
            response = sendMessage(mService, user, mimeMessage).getId();
        } catch (MessagingException e) {
            e.printStackTrace();
        }
        return response;
    }

    private MimeMessage createEmail(Set<String> to, String from, String subject, String bodyText) throws MessagingException, AddressException {
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);

        MimeMessage email = new MimeMessage(session);

        email.setFrom(new InternetAddress(from));
        for (String receiver : to) {
            email.addRecipient(javax.mail.Message.RecipientType.TO,
                    new InternetAddress(receiver));
        }
        email.setSubject(subject);
        email.setText(bodyText);
        return email;
    }

    private Message sendMessage(Gmail service, String userId, MimeMessage emailContent) throws MessagingException, IOException {
        Message message = createMessageWithEmail(emailContent);
        //Gmail.Users users = service.users();
        //Gmail.Users.Messages messages = users.messages();
        //Gmail.Users.Messages.Send send = messages.send(userId, message);
        //send.execute();
        service.users().messages().send(userId, message).execute();
        return message;
    }

    private Message createMessageWithEmail(MimeMessage emailContent) throws MessagingException, IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        emailContent.writeTo(buffer);
        byte[] bytes = buffer.toByteArray();
        String encodedEmail = Base64.encodeBase64URLSafeString(bytes);
        Message message = new Message();
        message.setRaw(encodedEmail);
        return message;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        taskManager.markAsCompleted(task);
        ScheduleTaskActivity scheduleTaskActivity = ScheduleTaskActivity.getInstance();
        if (scheduleTaskActivity != null) {
            scheduleTaskActivity.refresh();
        }
        task.setSendResult(s);
    }
}
package com.example.lhainan.simpleaccount;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.TextureView;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lhainan on 2015/12/12.
 */
public class RegisterActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private EditText cPasswordView;
    private View mLoginFormView;

    private UserRegisterTask mAuthTask = null;

    protected void onCreate(Bundle savedInstanceState) {
        Log.v("register", "here");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mEmailView = (AutoCompleteTextView)findViewById(R.id.register_email);
        populateAutoComplete();

        mEmailView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                View focusView = null;
                boolean cancel = false;
                String email = mEmailView.getText().toString();
                if (TextUtils.isEmpty(email)) {
                    mEmailView.setError("Please input the email!");
                    focusView = mEmailView;
                    cancel = true;
                }
                if (!TextUtils.isEmpty(email) && !isEmailValid(email)) {
                    mEmailView.setError(getString(R.string.error_field_required));
                    focusView = mEmailView;
                    cancel = true;
                }

            }
        });

        mPasswordView = (EditText) findViewById(R.id.register_password);
        cPasswordView = (EditText) findViewById(R.id.confirm_password);
        cPasswordView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                View focusView = null;
                boolean cancel = false;
                String password = mPasswordView.getText().toString();
                if (TextUtils.isEmpty(password)) {
                    mPasswordView.setError("Please input the password!");
                    focusView = mPasswordView;
                    cancel = true;
                }
                if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
                    mPasswordView.setError(getString(R.string.error_invalid_password));
                    focusView = mPasswordView;
                    cancel = true;
                }
            }
        });
        Button registerButton = (Button)findViewById(R.id.email_register_in_button);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email =  mEmailView.getText().toString();
                String password = mPasswordView.getText().toString();
                String confirm = cPasswordView.getText().toString();
                View focusView = null;
                boolean cancel = false;
                if (TextUtils.isEmpty(email)) {
                    mEmailView.setError("Please input the email!");
                    focusView =  mEmailView;
                    cancel = true;
                }
                if (!TextUtils.isEmpty(email) && !isEmailValid(email)) {
                    mEmailView.setError(getString(R.string.error_field_required));
                    focusView =  mEmailView;
                    cancel = true;
                }
                if (cancel) {
                    focusView.requestFocus();
                    return;
                }
                if (TextUtils.isEmpty(password)) {
                    mPasswordView.setError("Please input the password!");
                    focusView = mPasswordView;
                    cancel = true;
                }
                if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
                    mPasswordView.setError(getString(R.string.error_invalid_password));
                    focusView = mPasswordView;
                    cancel = true;
                }
                if (cancel) {
                    focusView.requestFocus();
                    return;
                }
                if (!password.equals(confirm)) {
                    cPasswordView.setError("the second password is not right");
                    mPasswordView.requestFocus();
                    return;
                }
                mAuthTask = new UserRegisterTask(email, password, v);
                mAuthTask.execute();
            }
        });
    }
    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        List<String> emails = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    private void populateAutoComplete() {
        getLoaderManager().initLoader(0, null, this);
    }

    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(RegisterActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mEmailView.setAdapter(adapter);
    }

    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }

    private String btoString(byte[] b){
        StringBuffer sb = new StringBuffer();
        for(int i = 0; i < b.length; i ++){
            sb.append(b[i]);
        }
        return sb.toString();
    }
    private boolean requestByPost(String name, String passwd) throws Throwable {
        String path = "http://1.xiaomiaoding.sinaapp.com/register.php";
// 请求的参数转换为byte数组
        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
        sha1.update(passwd.getBytes());
        byte[] sha1sum = sha1.digest();
        String params = "Name="+ name
                + "&Password="+ btoString(sha1sum);
        byte[] postData = params.getBytes();
        Log.v("params", params);
// 新建一个URL对象
        URL url = new URL(path);
// 打开一个HttpURLConnection连接
        HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
// 设置连接超时时间
        urlConn.setConnectTimeout(10 * 1000);
// Post请求必须设置允许输出
        urlConn.setDoOutput(true);
// Post请求不能使用缓存
        urlConn.setUseCaches(false);
// 设置为Post请求
        urlConn.setRequestMethod("POST");
        urlConn.setInstanceFollowRedirects(true);
// 配置请求Content-Type
        //           urlConn.setRequestProperty("Content-Type",
        //                   "application/x-www-form-urlencode");
// 开始连接
        urlConn.connect();
// 发送请求参数
        DataOutputStream dos = new DataOutputStream(urlConn.getOutputStream());
        dos.write(postData);
        dos.flush();
        dos.close();
// 判断请求是否成功
        if (urlConn.getResponseCode() == HttpURLConnection.HTTP_OK) {
// 获取返回的数据
            byte[] data = new byte[100];
            for (int i = 0; i < data.length; i++) {
                data[i] = 0;
            }
            //Log.v("content", urlConn.getContentEncoding());
            int len = urlConn.getInputStream().read(data);
            byte[] rdata = new byte[len];
            for (int i = 0; i < rdata.length; i++) {
                rdata[i] = data[i];
            }
            String response = new String(rdata, "UTF-8");
            urlConn.disconnect();
            Log.v("http error", response);
            if (response.equals("0"))
                return false;
            else if (response.equals("1"))
                return true;

        } else {
            urlConn.disconnect();
            Log.v("http error", ""+urlConn.getResponseCode());
            return false;
        }
        urlConn.disconnect();
        return false;
    }

    public class UserRegisterTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mPassword;
        private View loginview;
        UserRegisterTask(String email, String password, View v) {
            mEmail = email;
            mPassword = password;
            loginview = v;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                return requestByPost(mEmail, mPassword);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            Log.v("onPostExecute", success.toString());

            if (success) {
                finish();
                //Context context = loginview.getContext();
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                //intent.putExtra(ItemDetailFragment.ARG_ITEM_ID, "login");

                startActivity(intent);
            } else {
                mEmailView.setError("the email has already been registed!");
                mEmailView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
        }
    }
}


package com.example.memomaker;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.icu.util.Calendar;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import io.realm.Realm;

import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.Sort;


public class MainActivity extends AppCompatActivity implements SearchFragment.ParentFragmentListener {

    //ArrayAdapter<String> adapter;
    private LinearLayout rootView;
    private LayoutInflater inflater;
    //private LayoutInflater inflater_date;
    public int m_index = 0;
    public Realm realm;
    private static final int REQUEST_CHOOSER = 1000;
    //private Uri m_uri;
    private final int EXTERNAL_STORAGE_REQUEST_CODE = 1;
    private static final int RESULTCODE = 2;
    public static final String URI_DATA = "com.example.memomaker.MainActivity.DATA";
    //public Handler mHandler;
    //private ProgressDialog progressDialog;
    public SearchView mSearchView;
    public String m_search;
    //public Uri resultUri;
    //public Bitmap m_bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        /// パーミッション許可を取る
        if (Build.VERSION.SDK_INT >= 23) {
            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.READ_CONTACTS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_CONTACTS
                        },
                        EXTERNAL_STORAGE_REQUEST_CODE);
            }
        }

        Realm.init(this);
        realm = Realm.getDefaultInstance(); //Realmの初期化
        //RealmConfiguration config = new RealmConfiguration.Builder().build();
        //Realm.setDefaultConfiguration(config);

        rootView = findViewById(R.id.root);
        inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        //inflater_date = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        final EditText editText = (EditText) findViewById(R.id.edit_text);
        ImageButton saveButton = (ImageButton) findViewById(R.id.button2);
        ImageButton galleryButton = (ImageButton) findViewById(R.id.button3);
        //ImageButton cameraButton = (ImageButton) findViewById(R.id.button4);
        ScrollView scrollView = findViewById(R.id.scrollView);

        // 保存済みのメモがあれば読み込む
        RealmQuery<SampleModel> query = realm.where(SampleModel.class);
        RealmResults<SampleModel> result = query.findAll().sort("Id", Sort.ASCENDING);

        for (m_index = 0; m_index < result.size(); m_index++) {

            //View dateView = inflater_date.inflate(R.layout.date, null);
            //rootView.addView(dateView, rootView.getChildCount());
            if (result.get(m_index).getText() != null) {
                View subView = inflater.inflate(R.layout.sub, null);
                rootView.addView(subView, rootView.getChildCount());
                //先にボタンを表示させてからID取得(こうしないとエラーを吐くため)
                Button buttons = findViewById(R.id.text_button);
                buttons.setId(result.get(m_index).getId());
                //buttons.setId(m_index);
                buttons.setText(result.get(m_index).getText());

                TextView dateText = findViewById(R.id.date_text);
                TextView timeText = findViewById(R.id.time_text);
                dateText.setId(result.get(m_index).getId());
                //dateText.setId(m_index);

                //同一の日付を非表示する処理
                if (m_index > 0) {
                    String text = result.get(m_index - 1).getDate(); // 追加した日付の1つ前の日付を取得する
                    //同じ日付か確認
                    if (text.equals(result.get(m_index).getDate())) {
                        dateText.setVisibility(View.GONE);
                    } else {
                        dateText.setText(result.get(m_index).getDate());
                    }
                } else {
                    dateText.setText(result.get(m_index).getDate());
                }
                //dateText.setText(result.get(m_index).getDate());
                timeText.setId(result.get(m_index).getId());
                //timeText.setId(m_index);
                timeText.setText(result.get(m_index).getTime());

                buttons.setOnLongClickListener(new View.OnLongClickListener() {

                    @Override
                    public boolean onLongClick(View view) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setMessage("削除しますか？")
                                .setNeutralButton("コピー", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        RealmQuery<SampleModel> query = realm.where(SampleModel.class);
                                        query.equalTo("Id", buttons.getId());
                                        RealmResults<SampleModel> result = query.findAll().sort("Id", Sort.ASCENDING);
                                        //クリップボードに格納するItemを作成
                                        ClipData.Item item = new ClipData.Item(result.first().getText());
                                        //MIMETYPEの作成
                                        String[] mimeType = new String[1];
                                        mimeType[0] = ClipDescription.MIMETYPE_TEXT_URILIST;

                                        //クリップボードに格納するClipDataオブジェクトの作成
                                        ClipData cd = new ClipData(new ClipDescription("text_data", mimeType), item);

                                        //クリップボードにデータを格納
                                        ClipboardManager cm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                                        cm.setPrimaryClip(cd);
                                        Toast.makeText(MainActivity.this, "クリップボードにコピーしました", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        //DBの削除
                                        // トランザクション開始
                                        realm.beginTransaction();
                                        // 削除/更新したいテーブルデータを取得
                                        RealmQuery<SampleModel> query = realm.where(SampleModel.class);
                                        query.equalTo("Id", buttons.getId());
                                        RealmResults<SampleModel> result = query.findAll().sort("Id", Sort.ASCENDING);

                                        //今消そうとしてるDateがその日最後ならdateTextを表示から消す
                                        RealmQuery<SampleModel> findDate = realm.where(SampleModel.class);
                                        findDate.equalTo("date", result.first().getDate());
                                        RealmResults<SampleModel> findDateResult = findDate.findAll().sort("Id", Sort.ASCENDING);
                                        if (result.first().getId() == findDateResult.first().getId()) {
                                            if (findDateResult.size() > 1) {
                                                TextView dateText = findViewById(findDateResult.get(1).getId());
                                                //rootView.removeView(dateView);
                                                dateText.setVisibility(View.VISIBLE);
                                                dateText.setText(findDateResult.get(1).getDate());
                                            }
                                        }

                                        result.deleteAllFromRealm();
                                        realm.commitTransaction();

                                        View parentView = (View) view.getParent();// ボタンの親ビューを取得
                                        View removedView = (View) parentView.getParent();
                                        rootView.removeView(removedView);

                                        Toast.makeText(MainActivity.this, "削除しました", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .setNegativeButton("キャンセル", null)
                                .setCancelable(true);
                        // show dialog
                        builder.show();
                        return false;
                    }
                });
                scrollView.post(new Runnable() {
                    @Override
                    public void run() {
                        scrollView.fullScroll(View.FOCUS_DOWN);
                    }
                });


            } else {
                View subView = inflater.inflate(R.layout.imgbutton, null);
                rootView.addView(subView, rootView.getChildCount());
                //先にボタンを表示させてからID取得(こうしないとエラーを吐くため)
                ImageButton imageButton = findViewById(R.id.image_button);
                //byte[] image = result.get(m_index).getImage();
                String urist = result.get(m_index).getImage();
                imageButton.setId(result.get(m_index).getId());
                //buttons.setId(m_index);

                Uri uri = Uri.parse(result.get(m_index).getImage());

                if (uri == null) {
                    return;
                } else {
                    try {
                        imageButton.setImageURI(uri);
                    } catch(SecurityException e){
                        imageButton.setImageURI(null);
                    }
                }

                TextView dateText = findViewById(R.id.date_text);
                TextView timeText = findViewById(R.id.time_text);
                dateText.setId(result.get(m_index).getId());
                //dateText.setId(m_index);

                //同一の日付を非表示する処理
                if (m_index > 0) {
                    String text = result.get(m_index - 1).getDate(); // 追加した日付の1つ前の日付を取得する
                    //同じ日付か確認
                    if (text.equals(result.get(m_index).getDate())) {
                        dateText.setVisibility(View.GONE);
                    } else {
                        dateText.setText(result.get(m_index).getDate());
                    }
                } else {
                    dateText.setText(result.get(m_index).getDate());
                }
                //dateText.setText(result.get(m_index).getDate());
                timeText.setId(result.get(m_index).getId());
                //timeText.setId(m_index);
                timeText.setText(result.get(m_index).getTime());

                imageButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getApplicationContext(), ExpandImageViewActivity.class);
                        intent.putExtra(URI_DATA, urist);
                        startActivityForResult(intent, RESULTCODE);
                    }
                });

                imageButton.setOnLongClickListener(new View.OnLongClickListener() {

                    @Override
                    public boolean onLongClick(View view) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setMessage("削除しますか？")
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        //DBの削除
                                        // トランザクション開始
                                        realm.beginTransaction();
                                        // 削除/更新したいテーブルデータを取得
                                        RealmQuery<SampleModel> query = realm.where(SampleModel.class);
                                        query.equalTo("Id", imageButton.getId());
                                        RealmResults<SampleModel> result = query.findAll().sort("Id", Sort.ASCENDING);

                                        //今消そうとしてるDateがその日最後ならdateTextを表示から消す
                                        RealmQuery<SampleModel> findDate = realm.where(SampleModel.class);
                                        findDate.equalTo("date", result.first().getDate());
                                        RealmResults<SampleModel> findDateResult = findDate.findAll().sort("Id", Sort.ASCENDING);
                                        if (result.first().getId() == findDateResult.first().getId()) {
                                            if (findDateResult.size() > 1) {
                                                TextView dateText = findViewById(findDateResult.get(1).getId());
                                                //rootView.removeView(dateView);
                                                dateText.setVisibility(View.VISIBLE);
                                                dateText.setText(findDateResult.get(1).getDate());
                                            }
                                        }

                                        result.deleteAllFromRealm();
                                        realm.commitTransaction();

                                        View parentView = (View) view.getParent();// ボタンの親ビューを取得
                                        View removedView = (View) parentView.getParent();
                                        rootView.removeView(removedView);

                                        Toast.makeText(MainActivity.this, "削除しました", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .setNegativeButton("キャンセル", null)
                                .setCancelable(true);
                        // show dialog
                        builder.show();
                        return false;
                    }
                });
                scrollView.post(new Runnable() {
                    @Override
                    public void run() {
                        scrollView.fullScroll(View.FOCUS_DOWN);
                    }
                });

            }


        }


        // 保存ボタンをタップ時の処理
        saveButton.setOnClickListener(new View.OnClickListener() {
            //int index = 0;
            @Override
            public void onClick(View view) {
                // エディタの内容を取得します
                String memo = editText.getText().toString();

                // メモ内容が入力されていない場合は保存しないようにします
                if (memo.isEmpty()) {
                    // Toastを使ってメッセージを表示します
                    Toast.makeText(getApplicationContext(), "メモ内容が入力されていません", Toast.LENGTH_SHORT).show();
                    return;
                }

                //ボタンにテキストを出力しつつ表示
                //View dateView = inflater_date.inflate(R.layout.date, null);
                //rootView.addView(dateView, rootView.getChildCount());
                View subView = inflater.inflate(R.layout.sub, null);
                rootView.addView(subView, rootView.getChildCount());
                Button buttons = (Button) findViewById(R.id.text_button);


                //日付と時刻取得して表示
                Date d = new Date();
                Date t = new Date();
                SimpleDateFormat d2 = new SimpleDateFormat("yyyy年MM月dd日(E)");
                SimpleDateFormat t2 = new SimpleDateFormat("HH:mm");
                String date = d2.format(d);
                String time = t2.format(t);
                TextView dateText = findViewById(R.id.date_text);
                TextView timeText = findViewById(R.id.time_text);

                Number maxUserId = realm.where(SampleModel.class).max("Id");

                if (maxUserId != null) {
//                    RealmQuery<SampleModel> query = realm.where(SampleModel.class);
//                    RealmResults<SampleModel> result = query.findAll();
                    m_index = maxUserId.intValue() + 1;
                }
                buttons.setId(m_index);
                dateText.setId(m_index);
                timeText.setId(m_index);

                buttons.setText(memo);
                timeText.setText(time);

                //同じ日付なら日付を表示しない
                if (maxUserId != null) {
                    RealmQuery<SampleModel> query = realm.where(SampleModel.class);
                    query.equalTo("Id", m_index - 1);
                    RealmResults<SampleModel> result = query.findAll().sort("Id", Sort.ASCENDING);
                    String text = result.first().getDate(); // 追加した日付の1つ前の日付を取得する
                    //同じ日付か確認
                    if (text.equals(date)) {
                        dateText.setVisibility(View.GONE);
                    } else {
                        dateText.setText(date);
                    }
                } else {
                    dateText.setText(date);
                }


                // エディタはクリアします
                editText.setText("");

                // メモの内容を保存します

                //Realm書き込み

                realm.beginTransaction();
                SampleModel model = realm.createObject(SampleModel.class);
                model.setId(m_index);
                model.setText(memo);
                model.setDate(date);
                model.setTime(time);
                realm.commitTransaction();

                scrollView.post(new Runnable() {
                    @Override
                    public void run() {
                        scrollView.fullScroll(View.FOCUS_DOWN);
                    }
                });

                //生成したボタンに長押しイベントを付与する
                buttons.setOnLongClickListener(new View.OnLongClickListener() {

                    @Override
                    public boolean onLongClick(View view) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setMessage("削除しますか？")
                                .setNeutralButton("コピー", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        RealmQuery<SampleModel> query = realm.where(SampleModel.class);
                                        query.equalTo("Id", buttons.getId());
                                        RealmResults<SampleModel> result = query.findAll().sort("Id", Sort.ASCENDING);
                                        //クリップボードに格納するItemを作成
                                        ClipData.Item item = new ClipData.Item(result.first().getText());
                                        //MIMETYPEの作成
                                        String[] mimeType = new String[1];
                                        mimeType[0] = ClipDescription.MIMETYPE_TEXT_URILIST;

                                        //クリップボードに格納するClipDataオブジェクトの作成
                                        ClipData cd = new ClipData(new ClipDescription("text_data", mimeType), item);

                                        //クリップボードにデータを格納
                                        ClipboardManager cm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                                        cm.setPrimaryClip(cd);
                                        Toast.makeText(MainActivity.this, "クリップボードにコピーしました", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        //DBの削除
                                        // トランザクション開始
                                        realm.beginTransaction();
                                        // 削除/更新したいテーブルデータを取得
                                        RealmQuery<SampleModel> query = realm.where(SampleModel.class);
                                        query.equalTo("Id", buttons.getId());
                                        RealmResults<SampleModel> result = query.findAll().sort("Id", Sort.ASCENDING);
                                        //RealmResults<SampleModel> List = realm.where(SampleModel.class).findAll();

                                        //今消そうとしてるDateが先頭ならその一つ下のdateTextを非表示から表示にする。最後の1つならそのまま消す。
                                        RealmQuery<SampleModel> findDate = realm.where(SampleModel.class);
                                        findDate.equalTo("date", result.first().getDate());
                                        RealmResults<SampleModel> findDateResult = findDate.findAll().sort("Id", Sort.ASCENDING);
                                        if (result.first().getId() == findDateResult.first().getId()) {
                                            if (findDateResult.size() > 1) {
                                                TextView dateText = findViewById(findDateResult.get(1).getId());
                                                //rootView.removeView(dateView);
                                                dateText.setVisibility(View.VISIBLE);
                                                dateText.setText(findDateResult.get(1).getDate());
                                            }
                                        }


                                        result.deleteAllFromRealm();
                                        realm.commitTransaction();

                                        // update ListView
                                        // adapter.notifyDataSetChanged();
                                        View parentView = (View) view.getParent();// ボタンの親ビューを取得
                                        View removedView = (View) parentView.getParent();
                                        rootView.removeView(removedView);

                                        Toast.makeText(MainActivity.this, "削除しました", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .setNegativeButton("キャンセル", null)
                                .setCancelable(true);
                        // show dialog
                        builder.show();
                        return false;
                    }
                });
            }
        });

        // 画像ボタンをタップ時の処理
        galleryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showGallery();
            }

        });

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        // リスナーを削除します
        //realm.removeChangeListener(realmListener);
        // Realmインスタンスを閉じます
        realm.close();
    }


    @Override
    public void onRequestPermissionsResult(
            int requestCode, String[] permission, int[] grantResults
    ) {
        super.onRequestPermissionsResult(requestCode, permission, grantResults);
        if (grantResults.length <= 0) {
            return;
        }
        switch (requestCode) {
            case EXTERNAL_STORAGE_REQUEST_CODE: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // 許可が取れた場合
                    // 必要な処理を書いておく
                } else {
                    // 許可が取れなかった場合
                    // できれば終了させるよりも画像利用無しでメモ機能のみ利用できるようにしたい、要改善
                    Toast.makeText(this,
                            "許可が無ければカメラ・写真を利用できないためアプリを終了します", Toast.LENGTH_LONG).show();
                    finish();
                }
            }
            return;
        }
    }

    private void showGallery() {


        //カメラの起動Intentの用意
        //撮影を完了せずにキャンセル等をすると確保(Insert)場所にnull画像が残ってしまう問題のため現状未実装

//        String photoName = "com.example.memomaker" + System.currentTimeMillis() + ".jpg";
//        ContentValues contentValues = new ContentValues();
//        contentValues.put(MediaStore.Images.Media.TITLE, photoName);
//        contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/JPEG");
//        m_uri = getContentResolver()
//                .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
//        //m_uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
//
//        Intent intentCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        intentCamera.putExtra(MediaStore.EXTRA_OUTPUT, m_uri);

        // ギャラリー用のIntent作成
        Intent intentGallery;
        if (Build.VERSION.SDK_INT < 19) {
            intentGallery = new Intent(Intent.ACTION_GET_CONTENT);
            intentGallery.setType("image/*");
        } else {
            intentGallery = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intentGallery.addCategory(Intent.CATEGORY_OPENABLE);
            intentGallery.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intentGallery.setType("image/*");
        }
        //Intent intent = Intent.createChooser(intentCamera, "画像の選択");
        //intent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{intentGallery});
        startActivityForResult(intentGallery, REQUEST_CHOOSER);
    }

    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CHOOSER) {

            if (resultCode != RESULT_OK) {
                // キャンセル
//                if (m_uri != null) {
//                    getContentResolver().delete(m_uri, null, null);
//                    m_uri = null;
//                }
                return;
            }

            Uri resultUri = data.getData();
            //Uri resultUri = (data != null ? data.getData() : m_uri);

            if (resultUri == null) {
                // 取得失敗
//                if (m_uri != null) {
//                    getContentResolver().delete(m_uri, null, null);
//                    m_uri = null;
//                }
                return;
            }

            //URIのパーミッションを固定
            final int takeFlags = data.getFlags() & (Intent.FLAG_GRANT_READ_URI_PERMISSION
                    | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            getContentResolver().takePersistableUriPermission(resultUri, takeFlags);

            // ギャラリーへスキャンを促す
            MediaScannerConnection.scanFile(
                    this,
                    new String[]{resultUri.getPath()},
                    new String[]{"image/*"},
                    null
            );

            // 画像を設定
            View subView = inflater.inflate(R.layout.imgbutton, null);
            rootView.addView(subView, rootView.getChildCount());

            ImageButton imageButton = findViewById(R.id.image_button);

//            try {
//                m_bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), resultUri);
//                imageButton.setImageBitmap(m_bitmap);
//            }catch (IOException e) {
//                Toast.makeText(this, "画像が読み込めませんでした。", Toast.LENGTH_SHORT).show();
//                e.printStackTrace();
//            }

            imageButton.setImageURI(resultUri);


            //日付と時刻取得して表示
            Date d = new Date();
            Date t = new Date();
            SimpleDateFormat d2 = new SimpleDateFormat("yyyy年MM月dd日(E)");
            SimpleDateFormat t2 = new SimpleDateFormat("HH:mm");
            String date = d2.format(d);
            String time = t2.format(t);
            TextView dateText = findViewById(R.id.date_text);
            TextView timeText = findViewById(R.id.time_text);
            Number maxUserId = realm.where(SampleModel.class).max("Id");

            if (maxUserId != null) {
//                    RealmQuery<SampleModel> query = realm.where(SampleModel.class);
//                    RealmResults<SampleModel> result = query.findAll();
                m_index = maxUserId.intValue() + 1;
            }
            imageButton.setId(m_index);
            dateText.setId(m_index);
            timeText.setId(m_index);

            timeText.setText(time);

            //同じ日付なら2つ目以降の日付を表示しない
            if (maxUserId != null) {
                RealmQuery<SampleModel> query = realm.where(SampleModel.class);
                query.equalTo("Id", m_index - 1);
                RealmResults<SampleModel> result = query.findAll().sort("Id", Sort.ASCENDING);
                String text = result.first().getDate(); // 追加した日付の1つ前の日付を取得する
                //同じ日付か確認
                if (text.equals(date)) {
                    dateText.setVisibility(View.GONE);
                } else {
                    dateText.setText(date);
                }
            } else {
                dateText.setText(date);
            }



            //書き込み前にUriをStringに変換
            String StringUri = resultUri.toString();

            //Realm書き込み
            realm.beginTransaction();
            SampleModel model = realm.createObject(SampleModel.class);
            model.setId(m_index);
            model.setImage(StringUri);
            model.setDate(date);
            model.setTime(time);
            realm.commitTransaction();

            ScrollView scrollView = findViewById(R.id.scrollView);

            scrollView.post(new Runnable() {
                @Override
                public void run() {
                    scrollView.fullScroll(View.FOCUS_DOWN);
                }
            });

            //画像クリックで拡大表示
            imageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getApplicationContext(), ExpandImageViewActivity.class);
                    intent.putExtra(URI_DATA, StringUri);
                    startActivityForResult(intent, RESULTCODE);
                }
            });
            //長押し処理の付与
            imageButton.setOnLongClickListener(new View.OnLongClickListener() {

                @Override
                public boolean onLongClick(View view) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setMessage("削除しますか？")
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    //DBの削除
                                    // トランザクション開始
                                    realm.beginTransaction();
                                    // 削除/更新したいテーブルデータを取得
                                    RealmQuery<SampleModel> query = realm.where(SampleModel.class);
                                    query.equalTo("Id", imageButton.getId());
                                    RealmResults<SampleModel> result = query.findAll().sort("Id", Sort.ASCENDING);

                                    //今消そうとしてるDateが最後の一つならdateTextを表示から消す
                                    RealmQuery<SampleModel> findDate = realm.where(SampleModel.class);
                                    findDate.equalTo("date", result.first().getDate());
                                    RealmResults<SampleModel> findDateResult = findDate.findAll().sort("Id", Sort.ASCENDING);
                                    if (result.first().getId() == findDateResult.first().getId()) {
                                        if (findDateResult.size() > 1) {
                                            TextView dateText = findViewById(findDateResult.get(1).getId());
                                            //rootView.removeView(dateView);
                                            dateText.setVisibility(View.VISIBLE);
                                            dateText.setText(findDateResult.get(1).getDate());
                                        }
                                    }

                                    result.deleteAllFromRealm();
                                    realm.commitTransaction();

                                    View parentView = (View) view.getParent();// ボタンの親ビューを取得
                                    View removedView = (View) parentView.getParent();
                                    rootView.removeView(removedView);

                                    Toast.makeText(MainActivity.this, "削除しました", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .setNegativeButton("キャンセル", null)
                            .setCancelable(true);
                    // show dialog
                    builder.show();
                    return false;
                }
            });
        }

        if (requestCode == RESULTCODE && resultCode == RESULT_OK) {
            Toast.makeText(this, "元の画面に戻ってきた", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_activity_actions, menu);

        // ★  追加
        MenuItem menuItem = menu.findItem(R.id.action_search);
        mSearchView = (SearchView) menuItem.getActionView();
        mSearchView.setOnQueryTextListener(this.onQueryTextListener);
        return true;
    }


    private SearchView.OnQueryTextListener onQueryTextListener = new SearchView.OnQueryTextListener() {

        //SearchFragment mainFragment = new SearchFragment();
        @Override
        public boolean onQueryTextSubmit(String query) {
            // テキストを入力し、エンターを押した時呼び出される
            //buttonProcess();
            SearchFragment mainFragment = new SearchFragment();
            //FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            //ここで値渡しをするため検索処理をしておく
            //Realmの一致するテキストを探し出す
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            RealmQuery<SampleModel> query_realm = realm.where(SampleModel.class);
            m_search = query;
            if (query.length() > 0) {
                query_realm.contains("text", query); //部分一致検索
                RealmResults<SampleModel> result = query_realm.findAll().sort("Id", Sort.ASCENDING);
                ArrayList<String> items = new ArrayList<>();
                ArrayList<String> subItems = new ArrayList<>();

                for (int i = 0; i < result.size(); i++) {
                    items.add(result.get(i).getText());
                    subItems.add(result.get(i).getDate());
                }
                Bundle bundle = new Bundle();
                bundle.putStringArrayList("text", items);
                bundle.putStringArrayList("date", subItems);
                mainFragment.setArguments(bundle);
                transaction.replace(R.id.container, mainFragment);
            } else {
                transaction.replace(R.id.container, mainFragment);
                transaction.remove(mainFragment);
            }
            transaction.commit();
            return true;
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            // 文字が入力される度に呼び出される
            //ここで値渡しをするため検索処理をしておく
            //Realmの一致するテキストを探し出す
            SearchFragment mainFragment = new SearchFragment();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            RealmQuery<SampleModel> query_realm = realm.where(SampleModel.class);
            m_search = newText;
            if (newText.length() > 0) {
                query_realm.contains("text", newText); //部分一致検索
                RealmResults<SampleModel> result = query_realm.findAll().sort("Id", Sort.ASCENDING);
                ArrayList<String> items = new ArrayList<>();
                ArrayList<String> subItems = new ArrayList<>();

                for (int i = 0; i < result.size(); i++) {
                    items.add(result.get(i).getText());
                    subItems.add(result.get(i).getDate());
                }
                Bundle bundle = new Bundle();
                bundle.putStringArrayList("text", items);
                bundle.putStringArrayList("date", subItems);
                mainFragment.setArguments(bundle);
                //transaction.addToBackStack(null);
                transaction.replace(R.id.container, mainFragment); //replace増殖は起きていない
            } else {
                transaction.replace(R.id.container, mainFragment);
                transaction.remove(mainFragment);
            }
            transaction.commit();
            return true;
        }
    };

    @Override
    public void onClickButton(int position, String text) {
        //検索リストをクリックした後の処理
        RealmQuery<SampleModel> query_realm = realm.where(SampleModel.class);
        query_realm.contains("text", m_search); //部分一致検索
        RealmResults<SampleModel> result = query_realm.findAll().sort("Id", Sort.ASCENDING);
        View view = findViewById(result.get(position).getId());
        View parentView = (View) view.getParent();// ボタンの親ビューを取得
        //View removedView = (View) parentView.getParent();

        int y = parentView.getTop();
        ScrollView scrollView = this.findViewById(R.id.scrollView); //thisはいらないかも?
        scrollView.smoothScrollTo(0, y);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) { // if使うとエラー（itemがInt形式なため）
            case android.R.id.home:   // アプリアイコン（ホームアイコン）を押した時の処理
                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                break;
            case R.id.action_today:// カレンダーが押された時

                //Calendarインスタンスを取得
                final Calendar date = Calendar.getInstance();

                //DatePickerDialogインスタンスを取得
                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        MainActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                //日付を取得
                                String date = String.format("%d年%02d月%02d日", year, month + 1, dayOfMonth);
                                RealmQuery<SampleModel> query_realm = realm.where(SampleModel.class);
                                query_realm.contains("date", date);
                                RealmResults<SampleModel> result = query_realm.findAll().sort("Id", Sort.ASCENDING);
                                if (result.size() > 0) {
                                    View jumpView = findViewById(result.first().getId());
                                    View parentView = (View) jumpView.getParent();
                                    int y = parentView.getTop();
                                    ScrollView scrollView = findViewById(R.id.scrollView);
                                    scrollView.smoothScrollTo(0, y);
                                    Toast.makeText(MainActivity.this, "検索できました", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(MainActivity.this, "その日付にメモが存在しません", Toast.LENGTH_SHORT).show();
                                }
                            }
                        },
                        date.get(Calendar.YEAR),
                        date.get(Calendar.MONTH),
                        date.get(Calendar.DATE)
                );

                //dialogを表示
                datePickerDialog.show();
                break;
        }
        return true;
    }

}


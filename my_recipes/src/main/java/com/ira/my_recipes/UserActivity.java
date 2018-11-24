package com.ira.my_recipes;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.Manifest;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteAbortException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class UserActivity extends AppCompatActivity implements View.OnClickListener {

    EditText nameBox, linkBox, textBox;
    Button delButton, saveButton, editButton, btnLnk, btnTxt, btnImg, btnBack, saveImg, btnChoose;
    ImageView imageView;
    TextView textLink, textName, textText;

    DatabaseHelper sqlHelper;
    SQLiteDatabase db;
    Cursor userCursor;
    long userId=0;

    final int REQUEST_CODE_GALLERY = 999;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        init();

        sqlHelper = new DatabaseHelper(this);
        db = sqlHelper.getWritableDatabase();

        //кнопки разных способов добавления рецепта
        btnLnk.setOnClickListener(this);
        btnTxt.setOnClickListener(this);
        btnImg.setOnClickListener(this);

        // проверка каким способом был открыт активити: нажатием кнопки Добавить или кликом по пункту списка
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            userId = extras.getLong("id");
        }
        // если > 0 (клик по listview), то просмотр
        if (userId > 0) {
            // получаем элемент по id из бд
            userCursor = db.rawQuery("select * from " + DatabaseHelper.TABLE + " where " +
                    DatabaseHelper.COLUMN_ID + "=?", new String[]{String.valueOf(userId)});
            userCursor.moveToFirst();
            nameBox.setText(userCursor.getString(1));
            textName.setText(userCursor.getString(1));
            textLink.setText(userCursor.getString(2));
            linkBox.setText(userCursor.getString(2));
            textBox.setText(userCursor.getString(3));
            textText.setText(userCursor.getString(3));

            // если  заполнено поле ссылка, то остальные компоненты скрываем, то же самое и для рецепта, введенного вручную
            if (linkBox.getText().length() != 0)
            {
                linkBox.setVisibility(View.GONE);
                textBox.setVisibility(View.GONE);
                imageView.setVisibility(View.GONE);
                saveImg.setVisibility(View.GONE);

                textText.setVisibility(View.GONE);
                nameBox.setVisibility(View.GONE);
            }
            if (textBox.getText().length() != 0)
            {
                linkBox.setVisibility(View.GONE);
                imageView.setVisibility(View.GONE);
                saveImg.setVisibility(View.GONE);
                textLink.setVisibility(View.GONE);

                nameBox.setVisibility(View.GONE);
                textBox.setVisibility(View.GONE);
                textText.setMovementMethod(new ScrollingMovementMethod());
            }
            if (textBox.getText().length() == 0 && linkBox.getText().length() == 0)
            {
                btnChoose.setVisibility(View.VISIBLE);
                btnChoose.setEnabled(false);
                textBox.setVisibility(View.GONE);
                linkBox.setVisibility(View.GONE);
                saveButton.setVisibility(View.GONE);
                textLink.setVisibility(View.GONE);
                byte[] imgbyte = userCursor.getBlob(4);
                imageView.setImageBitmap(BitmapFactory.decodeByteArray(imgbyte, 0, imgbyte.length));

                nameBox.setVisibility(View.GONE);
                textText.setVisibility(View.GONE);
            }

            userCursor.close();

            // делаем неактивной кнопку Сохранить
            saveButton.setEnabled(false);
            saveImg.setEnabled(false);
            // скрываем кнопки добавления
            btnLnk.setVisibility(View.GONE);
            btnTxt.setVisibility(View.GONE);
            btnImg.setVisibility(View.GONE);
            // делаем недоступными для редактирования после перехода по клику на listview - возможен только просмотр
            noedt();
        }
        // если  0 (Добавить), то добавление
        else {
            // скрываем кнопку удаления
            delButton.setVisibility(View.GONE);
            // скрываем компоненты до момента пока не выбран способ добавления рецепта
            nameBox.setVisibility(View.GONE);
            linkBox.setVisibility(View.GONE);
            textLink.setVisibility(View.GONE);
            textBox.setVisibility(View.GONE);
            imageView.setVisibility(View.GONE);
            saveButton.setVisibility(View.GONE);
            saveImg.setVisibility(View.GONE);
            editButton.setVisibility(View.GONE);

            textText.setVisibility(View.GONE);
            textName.setVisibility(View.GONE);
        }

        btnChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityCompat.requestPermissions(
                        UserActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_CODE_GALLERY
                );
            }
        });
    }

    // выбор способа добавления рецепта
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnLnk: {
                nameBox.setVisibility(View.VISIBLE);
                linkBox.setVisibility(View.VISIBLE);
                saveButton.setVisibility(View.VISIBLE);
                btnLnk.setVisibility(View.GONE);
                btnTxt.setVisibility(View.GONE);
                btnImg.setVisibility(View.GONE);
                break;
            }
            case R.id.btnTxt: {
                nameBox.setVisibility(View.VISIBLE);
                textBox.setVisibility(View.VISIBLE);
                saveButton.setVisibility(View.VISIBLE);
                btnLnk.setVisibility(View.GONE);
                btnTxt.setVisibility(View.GONE);
                btnImg.setVisibility(View.GONE);
                break;
            }
            case R.id.btnImg: {
                btnChoose.setVisibility(View.VISIBLE);
                nameBox.setVisibility(View.VISIBLE);
                imageView.setVisibility(View.VISIBLE);
                saveImg.setVisibility(View.VISIBLE);
                btnLnk.setVisibility(View.GONE);
                btnTxt.setVisibility(View.GONE);
                btnImg.setVisibility(View.GONE);
                break;
            }
        }
    }

    // сохранение в БД при добавлении/редактировании ссылки либо введенного вручную текста
    public void save(View view){
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.COLUMN_NAME, nameBox.getText().toString());
        cv.put(DatabaseHelper.COLUMN_LINK, linkBox.getText().toString());
        cv.put(DatabaseHelper.COLUMN_TEXT, textBox.getText().toString());
        cv.put(DatabaseHelper.COLUMN_IMG, "");

        if (userId > 0) {
            db.update(DatabaseHelper.TABLE, cv, DatabaseHelper.COLUMN_ID + "=" + String.valueOf(userId), null);
        } else {
            db.insert(DatabaseHelper.TABLE, null, cv);
        }
        goHome();
    }

    // сохранение картинки в БД
    public void saveimg(View view){
        addEntryImg(nameBox.getText().toString().trim(), linkBox.getText().toString().trim(), textBox.getText().toString().trim(), imageViewToByte(imageView));
        goHome();
    }

    public void addEntryImg(String name, String link, String text, byte[] image) throws SQLiteAbortException {
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.COLUMN_NAME, name);
        cv.put(DatabaseHelper.COLUMN_LINK, link);
        cv.put(DatabaseHelper.COLUMN_TEXT, text);
        cv.put(DatabaseHelper.COLUMN_IMG, image);

        if (userId > 0) {
            db.update(DatabaseHelper.TABLE, cv, DatabaseHelper.COLUMN_ID + "=" + String.valueOf(userId), null);
        } else {
            db.insert(DatabaseHelper.TABLE, null, cv);
        }
    }

    // делаем поля доступными для редактирования после нажатия кнопки Edit
    public void edit(View view){
        edt();

        if (linkBox.getText().length() != 0)
        {
            linkBox.setVisibility(View.VISIBLE);
            nameBox.setVisibility(View.VISIBLE);
        }
        if (textBox.getText().length() != 0)
        {
            textBox.setVisibility(View.VISIBLE);
            nameBox.setVisibility(View.VISIBLE);
        }
        if (textBox.getText().length() == 0 && linkBox.getText().length() == 0)
        {
            nameBox.setVisibility(View.VISIBLE);
        }
        saveButton.setEnabled(true);
        saveImg.setEnabled(true);
        btnChoose.setEnabled(true);
    }

    // удаление рецепта из БД
    public void delete(View view){
        db.delete(DatabaseHelper.TABLE, "_id = ?", new String[]{String.valueOf(userId)});
        goHome();
    }

    // возврат к просмотру списка
    public void back(View view){
        finish();
    }

    private void goHome(){
        // закрываем подключение
        db.close();
        // переход к главной activity
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }


    private void init(){
        nameBox = (EditText) findViewById(R.id.name);
        linkBox = (EditText) findViewById(R.id.link);
        textBox = (EditText) findViewById(R.id.textBox);
        delButton = (Button) findViewById(R.id.deleteButton);
        saveButton = (Button) findViewById(R.id.saveButton);
        editButton = (Button) findViewById(R.id.editButton);
        saveImg = (Button) findViewById(R.id.saveImg);
        btnLnk = (Button) findViewById(R.id.btnLnk);
        btnTxt = (Button) findViewById(R.id.btnTxt);
        btnImg = (Button) findViewById(R.id.btnImg);
        btnBack = (Button) findViewById(R.id.btnBack);
        btnChoose = (Button) findViewById(R.id.btnChoose);
        imageView = (ImageView) findViewById(R.id.imageView);
        textLink = (TextView) findViewById(R.id.textLink);
        textName = (TextView) findViewById(R.id.textName);
        textText = (TextView) findViewById(R.id.textText);
    }

    private void noedt(){
        nameBox.setCursorVisible(false);
        nameBox.setLongClickable(false);
        linkBox.setCursorVisible(false);
        linkBox.setLongClickable(false);
        textBox.setCursorVisible(false);
        textBox.setLongClickable(false);
    }

    public void edt(){
        nameBox.setCursorVisible(true);
        nameBox.setLongClickable(true);
        linkBox.setCursorVisible(true);
        linkBox.setLongClickable(true);
        textLink.setVisibility(View.GONE);
        textName.setVisibility(View.GONE);
        textText.setVisibility(View.GONE);
        textBox.setCursorVisible(true);
        textBox.setLongClickable(true);
    }














    public static byte[] imageViewToByte(ImageView image) {
        Bitmap bitmap = ((BitmapDrawable)image.getDrawable()).getBitmap();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        return byteArray;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if(requestCode == REQUEST_CODE_GALLERY){
            if(grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, REQUEST_CODE_GALLERY);
            }
            else {
                Toast.makeText(getApplicationContext(), "You don't have permission to access file location!", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(requestCode == REQUEST_CODE_GALLERY && resultCode == RESULT_OK && data != null){
            Uri uri = data.getData();

            try {
                InputStream inputStream = getContentResolver().openInputStream(uri);

                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                imageView.setImageBitmap(bitmap);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }



}

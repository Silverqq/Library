package com.example.Library;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.example.notes.R;

import java.util.ArrayList;
public class MainActivity extends AppCompatActivity {
    private ListView listView;
    private ArrayAdapter adapter1;
    private BookAdapter adapter;
    static ArrayList<Book> dataItems;

    // Запуск активности для получения результата
    ActivityResultLauncher<Intent> mStartForResult2 = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        int updatedNum = data.getIntExtra("num", -1);
                        Book updatedBook = (Book) data.getSerializableExtra("updatedBook");

                        dataItems.add(updatedBook);

                        // Обновление адаптера
                        adapter.notifyDataSetChanged();
                    }
                }
            });
    ActivityResultLauncher<Intent> mStartForResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        int updatedNum = data.getIntExtra("num", -1);
                        Book updatedBook = (Book) data.getSerializableExtra("updatedBook");
                        boolean del = data.getBooleanExtra("del", false);

                        if (del){
                            dataItems.remove(updatedNum);
                        } else {
                            dataItems.set(updatedNum, updatedBook);
                        }

                        // Обновление адаптера
                        adapter.notifyDataSetChanged();
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Инициализация dataItems здесь
        dataItems = new ArrayList<>();
        DataBaseAccessor databaseAccessor = new DataBaseAccessor(this);
        dataItems.addAll(databaseAccessor.getAllData());
        adapter = new BookAdapter(this, android.R.layout.simple_expandable_list_item_1, dataItems);
        // Инициализация адаптера
        listView = findViewById(R.id.listView);
        listView.setAdapter(adapter);

        // Остальной код активности
        Button addButton = findViewById(R.id.addButton);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Book selecteBook = dataItems.get(i);
                Intent intent = new Intent(MainActivity.this, BookActivity.class);
                intent.putExtra("number", i);
                intent.putExtra("book", selecteBook);
                mStartForResult.launch(intent);
            }
        });

        //тоже для удаления(слушатель долгого нажатия)
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                showDeleteConfirmationDialog(i);
                return true;
            }
        });
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AddActivity.class);
                int num = dataItems.size() + 1;
                intent.putExtra("num", num);
                mStartForResult2.launch(intent);
            }
        });
    }


    //метод для удаления зажатием
    private void showDeleteConfirmationDialog(final int position) {
        DataBaseAccessor databaseAccessor = new DataBaseAccessor(this);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Удалить этот элемент?");
        builder.setPositiveButton("Удалить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Book selectedComputer = dataItems.get(position);
                databaseAccessor.deleteBook(selectedComputer.getId());
                dataItems.remove(position);
                adapter.notifyDataSetChanged();
                adapter.notifyDataSetChanged();
            }
        });
        builder.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();}
}
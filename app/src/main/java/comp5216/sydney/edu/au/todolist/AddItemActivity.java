package comp5216.sydney.edu.au.todolist;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class AddItemActivity extends AppCompatActivity {

    private EditText editTextItem;
    private EditText editTextAmount;
    private DatePicker datePicker;
    private Button btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);

        editTextItem = findViewById(R.id.editTextItem);
        editTextAmount = findViewById(R.id.editTextAmount);
        datePicker = findViewById(R.id.datePicker);
    }

    public void onSave(View v) {
        String item = editTextItem.getText().toString() + " (" + editTextAmount.getText().toString() + ")";
        String date = getDateFromDatePicker();

        Intent returnIntent = new Intent();
        returnIntent.putExtra("date", date);
        returnIntent.putExtra("item", item);

        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }

    private String getDateFromDatePicker() {
        int day = datePicker.getDayOfMonth();
        int month = datePicker.getMonth() + 1;
        int year = datePicker.getYear();
        return year + "-" + month + "-" + day;
    }
}

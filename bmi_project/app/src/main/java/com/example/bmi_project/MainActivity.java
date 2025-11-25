package com.example.bmi_project;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private EditText editHeight;
    private EditText editWeight;
    private RadioGroup genderGroup;
    private Button btnCalculate;
    private TextView textResult;
    private TextView textCategory;
    private ImageView imageCategory;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // ViewBinding aktif olacak şekilde View'ları bağla
        editHeight = findViewById(R.id.editHeight);
        editWeight = findViewById(R.id.editWeight);
        genderGroup = findViewById(R.id.genderGroup);
        btnCalculate = findViewById(R.id.btnCalculate);
        textResult = findViewById(R.id.textResult);
        textCategory = findViewById(R.id.textCategory);
        imageCategory = findViewById(R.id.imageCategory);

        // SharedPreferences başlat
        sharedPreferences = getSharedPreferences("BMIPrefs", MODE_PRIVATE);

        // Son 5 ölçümü yükle
        loadLastMeasurement();

        // Hesaplama işlemi için onClick listener
        btnCalculate.setOnClickListener(v -> calculateBMI());
    }

    private void calculateBMI() {
        String heightStr = editHeight.getText().toString();
        String weightStr = editWeight.getText().toString();

        // Boş girdi kontrolü
        if (heightStr.isEmpty() || weightStr.isEmpty()) {
            Toast.makeText(this, "Lütfen boy ve kilo giriniz", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Girdileri double'a çevir
            double height = Double.parseDouble(heightStr) / 100; // cm'den m'ye çevir
            double weight = Double.parseDouble(weightStr);

            // BMI hesapla: BMI = kilo / (boy)²
            double bmi = weight / (height * height);

            // Sonucu göster
            textResult.setText(String.format(Locale.getDefault(), "BMI = %.1f", bmi));

            // Kategori belirle ve göster
            String category = getCategory(bmi);
            int color = getCategoryColor(category);

            textCategory.setText(category);
            textCategory.setTextColor(color);

            // ImageView için kategori ikonu göster
            imageCategory.setVisibility(View.VISIBLE);
            imageCategory.setColorFilter(color);

            // Sonucu SharedPreferences'e kaydet
            saveMeasurement(bmi, category);

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Geçersiz sayı formatı", Toast.LENGTH_SHORT).show();
        }
    }

    private String getCategory(double bmi) {
        if (bmi < 18.5) {
            return "Zayıf";
        } else if (bmi < 25) {
            return "Normal";
        } else if (bmi < 30) {
            return "Fazla Kilolu";
        } else {
            return "Obez";
        }
    }

    private int getCategoryColor(String category) {
        switch (category) {
            case "Zayıf":
                return Color.parseColor("#2196F3"); // Mavi
            case "Normal":
                return Color.parseColor("#4CAF50"); // Yeşil
            case "Fazla Kilolu":
                return Color.parseColor("#FF9800"); // Turuncu
            case "Obez":
                return Color.parseColor("#F44336"); // Kırmızı
            default:
                return Color.BLACK;
        }
    }

    private void saveMeasurement(double bmi, String category) {
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Son 5 ölçümü kaydet
        for (int i = 4; i > 0; i--) {
            String prevBmi = sharedPreferences.getString("bmi_" + (i - 1), "");
            String prevCat = sharedPreferences.getString("cat_" + (i - 1), "");
            editor.putString("bmi_" + i, prevBmi);
            editor.putString("cat_" + i, prevCat);
        }

        // Yeni ölçümü kaydet
        editor.putString("bmi_0", String.format(Locale.getDefault(), "%.1f", bmi));
        editor.putString("cat_0", category);
        editor.apply();
    }

    private void loadLastMeasurement() {
        String lastBmi = sharedPreferences.getString("bmi_0", "");
        String lastCat = sharedPreferences.getString("cat_0", "");

        if (!lastBmi.isEmpty() && !lastCat.isEmpty()) {
            textResult.setText("BMI = " + lastBmi);
            textCategory.setText(lastCat);
            textCategory.setTextColor(getCategoryColor(lastCat));
            imageCategory.setVisibility(View.VISIBLE);
            imageCategory.setColorFilter(getCategoryColor(lastCat));
        }
    }
}
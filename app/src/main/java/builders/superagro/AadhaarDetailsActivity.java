package builders.superagro;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class AadhaarDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aadhaar_details);
        findViewById(R.id.confirmButton).setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                AadhaarDetailsActivity.this.startActivity(new Intent(AadhaarDetailsActivity.this,FarmSelect.class));
            }
        });
    }
}

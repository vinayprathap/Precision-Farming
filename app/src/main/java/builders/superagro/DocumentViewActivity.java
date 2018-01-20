package builders.superagro;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import static builders.superagro.R.id.apply;

public class DocumentViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_document_view);
        findViewById(apply).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                apply(v);
            }
        });
    }
    void apply(View v){
        Intent i = new Intent(this,FinalActivity.class);
        startActivity(i);

    }
}

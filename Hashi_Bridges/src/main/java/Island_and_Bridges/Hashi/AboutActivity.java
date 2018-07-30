package Island_and_Bridges.Hashi;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class AboutActivity extends Activity {

    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        TextView textView = new TextView(this);
        setContentView(R.layout.about);
        textView.findViewById(R.id.text);
        textView.setText("Test");
        textView.setVisibility(View.VISIBLE);
    }

}

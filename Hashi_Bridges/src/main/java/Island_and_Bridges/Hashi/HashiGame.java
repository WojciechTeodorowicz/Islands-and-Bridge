/**
 *
 */
package Island_and_Bridges.Hashi;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

//import com.amazonaws.mobile.client.AWSMobileClient;
//import com.amazonaws.mobile.client.AWSStartupHandler;
//import com.amazonaws.mobile.client.AWSStartupResult;

import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.TimerTask;

/**
 * @author ghb
 *
 */
public class HashiGame extends Activity implements OnClickListener {


  public static final String KEY_DIFFICULTY =
    "de.Hashi.difficulty";

  // Commands
  public static final int RESTART = 1;
  public static final int EXIT = 2;
  public static final int TIME = 3;
  public static final int DIFFICULTY_EASY = 0;
  public static final int DIFFICULTY_MEDIUM = 1;
  public static final int DIFFICULTY_HARD = 2;

  private BoardCreation boardstate;
  private MapView view;

  private void ResetGameState(boolean reset_timer) {
    int diff = getIntent().getIntExtra(KEY_DIFFICULTY, DIFFICULTY_EASY);
    BoardCreation oldstate = (BoardCreation) getLastNonConfigurationInstance();
    if (boardstate == null && oldstate == null) {
      Log.d(getClass().getName(), "no old boardstate ");
      boardstate = new BoardCreation(diff);
    }
    if (boardstate == null && oldstate != null) {
      boardstate = oldstate;
    }
    if (boardstate != null && oldstate == null) {
      boardstate.ResetGame();
    }
    if (reset_timer) {

      gametimer.purge();
    }
  }


  @Override
  public Object onRetainNonConfigurationInstance() {
      return boardstate;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    //TODO connect to AWS mobile for online leaderboard
      //AWSMobileClient.getInstance().initialize(this, new AWSStartupHandler() {
        //  @Override
          //public void onComplete(AWSStartupResult awsStartupResult) {
            //  Log.d("YourMainActivity", "AWSMobileClient is instantiated and you are connected to AWS!");
          //}
      //}).execute();
    ResetGameState(true);

    view = new MapView(this, boardstate);
    LinearLayout l = new LinearLayout(this);
    l.setOrientation(LinearLayout.VERTICAL);

    // Create a LinearLayout in which to add the ImageView
    LinearLayout lheader = new LinearLayout(this);
    lheader.setOrientation(LinearLayout.HORIZONTAL);

    TextView time = new TextView(this);
    time.setId(TIME);
    lheader.addView(time);

    Button b = new Button(this);
    b.setText("Restart");
    b.setId(RESTART);
    b.setOnClickListener(this);
    lheader.addView(b);

    b = new Button(this);
    b.setText("Exit");
    b.setId(EXIT);
    b.setOnClickListener(this);
    lheader.addView(b);
    l.addView(lheader);

    l.addView(view);
    // Add the ImageView to the layout and set the layout as the content view
    setContentView(l);

    gametimer.schedule(new TimerTask() {
      public void run() {
        seconds += 0.1;
        hRefresh.sendEmptyMessage(0);
      }
    }, 100, 100);

    view.requestFocus();
  }

  Handler hRefresh = new Handler(){
    @Override
      public void handleMessage(Message msg) {
	updatetime();
      }
  };

  float seconds = 0;

  java.util.Timer gametimer = new java.util.Timer();

  public static String format(double x, int width, int precision) {
    StringBuffer strBuf = new StringBuffer();
    String str = new String();
    DecimalFormat decForm =
      (DecimalFormat) NumberFormat.getInstance(Locale.ENGLISH);
    FieldPosition intPos = new FieldPosition(DecimalFormat.INTEGER_FIELD);
    decForm.setMinimumFractionDigits(precision);
    decForm.setMaximumFractionDigits(precision);
    decForm.format(x, strBuf, intPos);
    for (int i = 0; i < width - strBuf.length(); i++) {
      str += " ";
    }
    return (str + strBuf);
  }

  void updatetime() {
    TextView t = (TextView)findViewById(TIME);
    t.setText("Elapsed Time: "+ format(seconds, 5, 1 ));
    t.postInvalidate();
  }


  @Override
    public void onClick(View view) {
      switch (view.getId()) {
	case RESTART:
	  ResetGameState(true);
	  this.view.Reset();
	  this.view.invalidate();
	  break;
	case EXIT:
	  ResetGameState(true);
	  finish();
	  break;
      }
    }

  @Override
    protected void onResume() {
      // Load sate
      super.onResume();
    }

  @Override
    protected void onPause() {
      // Store sate
      super.onPause();
    }

}

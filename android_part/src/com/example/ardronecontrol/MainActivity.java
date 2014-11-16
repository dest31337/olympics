package com.example.ardronecontrol;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.app.Activity;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.MobileAnarchy.Android.Widgets.Joystick.*;
import android.os.Handler;
import android.os.Environment;

import java.io.*;
import java.net.MalformedURLException;
import java.util.Date;

public class MainActivity extends Activity {

    // Чувствительность дрона (на это число делится диапазон скоростей, чтобы дрон не летал слишком быстро)
    // например если скорость вперед 1.0, а SENSITIVITY = 3, то финальная скорость дрона которая уйдет на сервер
    // будет составлять 0.3
    private float SENSITIVITY = 3;

    private JoystickView jvLeft;
    private JoystickView jvRight;
    private SharedPreferences sp;
    private Drone drone;

    private Handler mHandler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        getActionBar().setDisplayShowHomeEnabled(false);
        getActionBar().setDisplayShowTitleEnabled(false);

		setContentView(R.layout.activity_main);
        start();
	}

    /**
     * Инициализация свойств
     */
    private void start() {
        sp = PreferenceManager.getDefaultSharedPreferences(this);
        drone = new Drone(sp.getString("address",getResources().getString(R.string.default_address)),this, sp);
        jvLeft = (JoystickView)findViewById(R.id.jvLeft);
        jvRight = (JoystickView)findViewById(R.id.jvRight);

        jvLeft.setYAxisInverted(false);
        jvLeft.setOnJostickMovedListener(_listener);

        jvRight.setYAxisInverted(false);
        jvRight.setOnJostickMovedListener(_listener);
        drone.start();
    }

    /**
     * стандартный метод activity - когда главная форма возвращается на экран после сна (скрытия)
     */
    protected void onResume() {
        //перезапись настроек, если необходимо
        //if (myTimer!=null) restart();
        super.onResume();
    }

    /**
     * Для реализации формы настроек
     */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mainmenu, menu);
        menu.findItem(R.id.settings).setIntent(new Intent(this, SettingsActivity.class));
        return true;
    }

    /**
     * Метод прослушивающий движения джойстика и оповещающий дрона о необходимых действиях
     */
    protected void onJoystickMoved(){
        try {
            SENSITIVITY = Float.parseFloat(sp.getString("mul",getResources().getString(R.string.default_mul)));
        } catch (Exception exc) {
            SENSITIVITY = 3;
        }
        double leftRight = jvRight.getUserX() / (float) JoystickView.COORDINATE_MAX / (float) SENSITIVITY;
        double frontBack = jvRight.getUserY() / (float) JoystickView.COORDINATE_MAX / (float) SENSITIVITY;
        double upDown = jvLeft.getUserY() / (float) JoystickView.COORDINATE_MAX;
        double clockWise = jvLeft.getUserX() / (float) JoystickView.COORDINATE_MAX;

        // округление - если меньше чем 0.05 то округляем до 0
        if (Math.abs(leftRight) < 0.05) {
            leftRight = 0;
        }
        if (Math.abs(frontBack) < 0.05) {
            frontBack = 0;
        }
        if (Math.abs(upDown) < 0.05) {
            upDown = 0;
        }
        if (Math.abs(clockWise) < 0.05) {
            clockWise = 0;
        }
        if(leftRight == 0 && frontBack == 0 && upDown == 0 && clockWise == 0) {
            drone.stop();
        } else {
            drone.move(leftRight, frontBack, upDown, clockWise);
        }
    }

    /**
     * реализация интерфейса прослушивания джойстика
     */
    private JoystickMovedListener _listener = new JoystickMovedListener() {
        public void OnMoved(int pan, int tilt) {
            onJoystickMoved();
        }
        public void OnReleased() {}
        public void OnReturnedToCenter() {
            onJoystickMoved();
        }
    };

    /**
     * обработка нажатия меню (в верхней части экрана)
     * @param menuItem выбранный пункт меню (кнопка)
     * @return
     * @throws MalformedURLException
     */
    public boolean onMenuItemClick(MenuItem menuItem) throws MalformedURLException {
        switch (menuItem.getItemId()) {
            case R.id.takeoff:
                drone.takeOff();
                break;
            case R.id.land:
                drone.land();
                break;
            case R.id.disableemergency:
                drone.disableEmergency();
                break;
            case R.id.switch_cam:
                drone.switchCamera();
                break;
            case R.id.saveimg:
                LinearLayout mainLayout = (LinearLayout) findViewById(R.id.mainLayout);
                Drawable currentImage = mainLayout.getBackground();

                String path = Environment.getExternalStorageDirectory().toString();
                OutputStream fOut = null;
                File file = new File(path, "DronePicture"+new Date().toString()+".jpg");
                if (currentImage != null) {
                    try {
                        file.createNewFile();
                        fOut = new FileOutputStream(file);
                        Bitmap bm = ((BitmapDrawable) currentImage).getBitmap();
                        bm.compress(Bitmap.CompressFormat.JPEG, 85, fOut);
                        fOut.flush();
                        fOut.close();
                    } catch (IOException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                }
                break;
        }
        // метод должен возвращать значение, но нам оно не нужно
        return true;
    }
}
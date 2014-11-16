package com.example.ardronecontrol;

import android.app.Activity;
import android.content.SharedPreferences;
import android.util.Log;
import android.graphics.drawable.Drawable;
import android.os.Message;
import android.widget.LinearLayout;
import android.os.Handler;
import android.widget.TextView;

/**
 * Created with IntelliJ IDEA.
 * User: Vlad Lebedintsev
 * Date: 25.05.13
 * Time: 10:41
 * Класс реализующий управление квадрокоптером Parrot ARDrone 2.0 через сервер на базе модуля ar-drone
 * для Node.JS
 */
public class Drone implements Runnable {
    private String address;
    private Activity activity;
    private SharedPreferences sp;
    LinearLayout mainLayout;
    TextView batText;
    TextView qrText;
    public boolean Active=false;
    private int cameraId = 0; //0 or 3
    Handler h;
    Handler batteryHandler;
    Handler qrHandler;

    // строка - структура предыдущей команды на перемещение. Если очередная команда с ней совпадает, данные не посылаются.
    private String lastMoveData = "";

    // признак перемещения дрона. Если дрон не перемещается команда stop не посылается
    private boolean isMoving = false;

    /**
     * Конструктор
     * @param address - ip-адрес сервера с Node.JS
     * @param activity - основная Activity для доступа к ресурсам приложения
     * @param sp - настройки приложения
     */

    public Drone(String address, Activity activity, SharedPreferences sp) {
        this.address=address;
        this.activity = activity;
        this.sp = sp;
        mainLayout = (LinearLayout) activity.findViewById(R.id.mainLayout);
        batText = (TextView) activity.findViewById(R.id.battery);
        qrText = (TextView) activity.findViewById(R.id.qr);
    }

    /**
     * Функция проверки факта движения дрона
     * @return true если дрон выполняет команду move в текущий момент, иначе false
     */
    public boolean isMoving(){
        return isMoving;
    }

    /**
     * Команда на взлет
     */
    public void takeOff() {
        new Thread(new SendGetRequest(address,sp,activity.getResources().getString(R.string.take_off_command))).start();
    }

    /**
     * Команда на посадку
     */
    public void land() {
        new Thread(new SendGetRequest(address,sp,activity.getResources().getString(R.string.land_command))).start();
    }

    /**
     * Команда на сброс аварийной ситуации (возникает при аварийных посадках после столкновений и .т.п, если
     * аварийная ситуация не сброшена, дрон на остальные команды не реагирует)
     */
    public void disableEmergency() {
        new Thread(new SendGetRequest(address,sp,activity.getResources().getString(R.string.disable_emergency_command))).start();
    }

    /**
     * Команда переключения камеры с основной на нижнюю и обратно.
     */
    public void switchCamera() {
        new Thread(new SendGetRequest(address,sp,activity.getResources().getString(R.string.switch_camera)+"?key=video:video_channel&value="+cameraId)).start();
        cameraId = cameraId==0? 3 : 0;
    }

    /**
     * Запускает поток обращений к серверу за графической информацией
     */
    public void start() {
        h = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                mainLayout.setBackground((Drawable) msg.obj);
            }
        };
        batteryHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                batText.setText(msg.obj.toString().equals("-1")?"Battery inf is unreachable":("Dron battery: "+msg.obj.toString()+"%"));
            }
        };
        qrHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                qrText.setText(msg.obj.toString().equals("null")?"There were not any QR-codes":("Last parse QR-code: "+msg.obj.toString()));
            }
        };
        new Thread(this).start();
    }

    /**
     * Основной метод интерфейса Runnable - реализует получение графической информации в потоке
     */
    public void run() {
        Active = true;
        long sleepTime = 0;
        while (Active) {
            address = sp.getString("address",activity.getResources().getString(R.string.default_address));
            try {
                sleepTime = Long.parseLong(sp.getString("refresh_scr_freq",activity.getResources().getString(R.string.default_refresh_freq)));
            } catch (Exception exc) {
                sleepTime = 100;
            }
            SendGetRequest sgr = new SendGetRequest(address,sp);
            sgr.setmHandler(h);
            sgr.doInBackground(activity.getResources().getString(R.string.getimage_command));

            SendGetRequest sgr1 = new SendGetRequest(address,sp);
            sgr1.setmHandler(batteryHandler);
            sgr1.doInBackground(activity.getResources().getString(R.string.battery_command));

            SendGetRequest sgr2 = new SendGetRequest(address,sp);
            sgr2.setmHandler(qrHandler);
            sgr2.doInBackground(activity.getResources().getString(R.string.qr_command));

            try {
                Thread.currentThread().sleep(sleepTime);
            } catch (InterruptedException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
    }

    /**
     * Команда остановки. Получив ее дрон, совершающий движение зависнет на месте.
     */
    public void stop() {
        if (isMoving) {
          new Thread(new SendGetRequest(address,sp,activity.getResources().getString(R.string.stop_command))).start();
          isMoving = false;
          Log.d("SEND_COMMAND", "STOP");
        }
    }

    /**
     * Команда начать движение или изменить направление или скорость движения дрона.
     * @param x - скорость движения вперед или назад (диапазон значений от -1.0 (полный назад) до 1.0 (полный вперед))
     * @param y - скорость движения влево или вправо (диапазон значений от -1.0 (полный влево) до 1.0 (полный вправо))
     * @param z - скорость движения вверх или вниз (диапазон значений от -1.0 (полный вниз) до 1.0 (полный вверх))
     * @param rotate - скорость вращения дрона вокруг своей оси (диапазон значений от -1.0 (против часовой) до 1.0 (по часовой))
     */
    public void move(double x, double y, double z, double rotate) {
        // строка с командами движения для отправки на сервер
        String data = String.format("x=%.1f&y=%.1f&z=%.1f&rotate=%.1f",x,y,z,rotate);
        // сравним с предыдущей отправленной строкой для принятия решения об отправке на сервер
        if (!lastMoveData.contentEquals(data) || !isMoving) {
          new Thread(new SendGetRequest(address,sp,activity.getResources().getString(R.string.move_command)+data)).start();
          isMoving = true;
          lastMoveData = data;
          Log.d("SEND_COMMAND", data);
        }
    }
}

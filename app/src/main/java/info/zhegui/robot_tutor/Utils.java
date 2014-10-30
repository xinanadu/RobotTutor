package info.zhegui.robot_tutor;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by ASUS on 2014/10/30.
 */
public class Utils {
    private static Toast toast;

    public static void toast(Context context, String text) {
        if (toast == null) {
            toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
        } else {
            toast.setText(text);
            toast.setDuration(Toast.LENGTH_SHORT);
        }
        toast.show();
    }
}

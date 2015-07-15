package pond.common;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * This util class is build for basic type
 */
public class Convert {

    public static Integer toInt(String str) {
        return Integer.parseInt(str);
    }

    public static Long toLong(String str) {
        return Long.parseLong(str);
    }

    public static Double toDouble(String str) {
        return Double.parseDouble(str);
    }

    public static Date toDate(String str, String format) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.parse(str);
    }

    public static Date toDate(Long longstr){
        return new Date(longstr);
    }

    public static String toString(Date date, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(date);
    }

    public static Long dateToLong(String date, String format) throws ParseException {
        Date parsed = new SimpleDateFormat(format).parse(date);
        return parsed.getTime();
    }

    /**
     * <p>
     * WARNING!!! ONLY POSITIVE VALUES WILL BE RETURN
     * </p>
     *
     * @param value input value
     * @return above zero
     */
    public static int toUnsigned(String value) {
        int ret = 0;
        if (value == null || value.isEmpty()) {
            return 0;
        }
        char tmp;
        for (int i = 0; i < value.length(); i++) {
            tmp = value.charAt(i);
            if (!Character.isDigit(tmp)) {
                return 0;
            }
            ret = ret * 10 + ((int) tmp - (int) '0');
        }
        return ret;
    }
}

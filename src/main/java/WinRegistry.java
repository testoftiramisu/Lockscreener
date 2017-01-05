import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.prefs.Preferences;

final class WinRegistry {
    private static final int ERROR_CODE = 1;
    private static final int HKEY_CURRENT_USER = 0x80000001;
    static final int HKEY_LOCAL_MACHINE = 0x80000002;
    private static final int REG_SUCCESS = 0;
    private static final int REG_NOTFOUND = 2;
    private static final int REG_ACCESSDENIED = 5;
    private static final int KEY_ALL_ACCESS = 0xf003f;
    private static final int KEY_READ = 0x20019;
    private static final Preferences USER_ROOT = Preferences.userRoot();
    private static final Preferences SYSTEM_ROOT = Preferences.systemRoot();
    private static final Class<? extends Preferences> USER_CLASS = USER_ROOT.getClass();
    private static final Method REG_OPEN_KEY;
    private static final Method REG_CLOSE_KEY;
    private static final Method REG_QUERY_VALUE_EX;
    private static final Method REG_SET_VALUE_EX;
    private static final Method REG_CREATE_KEY_EX;

    static {
        try {
            REG_OPEN_KEY = USER_CLASS.getDeclaredMethod("WindowsRegOpenKey", int.class, byte[].class, int.class);
            REG_OPEN_KEY.setAccessible(true);

            REG_CLOSE_KEY = USER_CLASS.getDeclaredMethod("WindowsRegCloseKey", int.class);
            REG_CLOSE_KEY.setAccessible(true);

            REG_QUERY_VALUE_EX = USER_CLASS.getDeclaredMethod("WindowsRegQueryValueEx", int.class, byte[].class);
            REG_QUERY_VALUE_EX.setAccessible(true);

            REG_SET_VALUE_EX = USER_CLASS.getDeclaredMethod("WindowsRegSetValueEx", int.class, byte[].class, byte[].class);
            REG_SET_VALUE_EX.setAccessible(true);

            REG_CREATE_KEY_EX = USER_CLASS.getDeclaredMethod("WindowsRegCreateKeyEx", int.class, byte[].class);
            REG_CREATE_KEY_EX.setAccessible(true);

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private WinRegistry() {
    }

    /**
     * Read a value from key and value name.
     *
     * @param hkey      HKEY_CURRENT_USER/HKEY_LOCAL_MACHINE
     * @param key
     * @param valueName
     * @return the value
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    public static String readString(int hkey, String key, String valueName)
            throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        if (hkey == HKEY_LOCAL_MACHINE) {
            return readString(SYSTEM_ROOT, hkey, key, valueName);
        } else if (hkey == HKEY_CURRENT_USER) {
            return readString(USER_ROOT, hkey, key, valueName);
        } else {
            throw new IllegalArgumentException("hkey=" + hkey);
        }
    }

    private static String readString(Preferences root, int hkey, String key, String value)
            throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        int[] handles = (int[]) REG_OPEN_KEY.invoke(root, hkey, stringToByteArray(key), KEY_READ);

        if (handles[ERROR_CODE] != REG_SUCCESS) {
            return null;
        }

        byte[] valb = (byte[]) REG_QUERY_VALUE_EX.invoke(root, handles[0], stringToByteArray(value));
        REG_CLOSE_KEY.invoke(root, handles[0]);
        return (valb != null ? new String(valb).trim() : null);
    }

    /**
     * Write a value in a given key/value name.
     *
     * @param hkey
     * @param key
     * @param valueName
     * @param value
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    static void writeStringValue(int hkey, String key, String valueName, String value)
            throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        if (hkey == HKEY_LOCAL_MACHINE) {
            writeStringValue(SYSTEM_ROOT, hkey, key, valueName, value);
        } else if (hkey == HKEY_CURRENT_USER) {
            writeStringValue(USER_ROOT, hkey, key, valueName, value);
        } else {
            throw new IllegalArgumentException("hkey=" + hkey);
        }
    }


    private static void writeStringValue(Preferences root, int hkey, String key, String valueName, String value)
            throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        int[] handles = (int[]) REG_OPEN_KEY.invoke(root, hkey, stringToByteArray(key), KEY_ALL_ACCESS);
        int rc;

        if (handles[ERROR_CODE] == REG_ACCESSDENIED) {
            throw new IllegalAccessException("Key " + key + " cannot be opened. Access denied.\n" + "Try to re-run application with Administrator privileges.");
        } else {
            rc = (Integer) REG_SET_VALUE_EX.invoke(root, handles[0], stringToByteArray(valueName), stringToByteArray(value));
            if (rc == REG_SUCCESS) {
                REG_CLOSE_KEY.invoke(root, handles[0]);
            }
        }
    }

    private static int[] createKey(Preferences root, int hkey, String key)
            throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        return (int[]) REG_CREATE_KEY_EX.invoke(root, hkey, stringToByteArray(key));
    }

    /**
     * Returns this java string as a null-terminated byte array.
     */
    private static byte[] stringToByteArray(String str) {
        byte[] result = new byte[str.length() + 1];

        for (int i = 0; i < str.length(); i++) {
            result[i] = (byte) str.charAt(i);
        }
        result[str.length()] = 0;
        return result;
    }
}

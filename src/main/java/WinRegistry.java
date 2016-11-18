import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;

public class WinRegistry {
    private static final int ERROR_CODE = 1;
    public static final int HKEY_CURRENT_USER = 0x80000001;
    public static final int HKEY_LOCAL_MACHINE = 0x80000002;
    public static final int REG_SUCCESS = 0;
    public static final int REG_NOTFOUND = 2;
    public static final int REG_ACCESSDENIED = 5;

    private static final int KEY_ALL_ACCESS = 0xf003f;
    private static final int KEY_READ = 0x20019;
    private static final Preferences userRoot = Preferences.userRoot();
    private static final Preferences systemRoot = Preferences.systemRoot();
    private static final Class<? extends Preferences> userClass = userRoot.getClass();
    private static final Method regOpenKey;
    private static final Method regCloseKey;
    private static final Method regQueryValueEx;
    private static final Method regEnumValue;
    private static final Method regQueryInfoKey;
    private static final Method regEnumKeyEx;
    private static final Method regCreateKeyEx;
    private static final Method regSetValueEx;
    private static final Method regDeleteKey;
    private static final Method regDeleteValue;

    static {
        try {
            regOpenKey = userClass.getDeclaredMethod("WindowsRegOpenKey", int.class, byte[].class, int.class);
            regOpenKey.setAccessible(true);

            regCloseKey = userClass.getDeclaredMethod("WindowsRegCloseKey", int.class);
            regCloseKey.setAccessible(true);

            regQueryValueEx = userClass.getDeclaredMethod("WindowsRegQueryValueEx", int.class, byte[].class);
            regQueryValueEx.setAccessible(true);

            regEnumValue = userClass.getDeclaredMethod("WindowsRegEnumValue", int.class, int.class, int.class);
            regEnumValue.setAccessible(true);

            regQueryInfoKey = userClass.getDeclaredMethod("WindowsRegQueryInfoKey1", int.class);
            regQueryInfoKey.setAccessible(true);

            regEnumKeyEx = userClass.getDeclaredMethod("WindowsRegEnumKeyEx", int.class, int.class, int.class);
            regEnumKeyEx.setAccessible(true);

            regCreateKeyEx = userClass.getDeclaredMethod("WindowsRegCreateKeyEx", int.class, byte[].class);
            regCreateKeyEx.setAccessible(true);

            regSetValueEx = userClass.getDeclaredMethod("WindowsRegSetValueEx", int.class, byte[].class, byte[].class);
            regSetValueEx.setAccessible(true);

            regDeleteValue = userClass.getDeclaredMethod("WindowsRegDeleteValue", int.class, byte[].class);
            regDeleteValue.setAccessible(true);

            regDeleteKey = userClass.getDeclaredMethod("WindowsRegDeleteKey", int.class, byte[].class);
            regDeleteKey.setAccessible(true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private WinRegistry() {
    }

    /**
     * Read a value from key and value name
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
            return readString(systemRoot, hkey, key, valueName);
        } else if (hkey == HKEY_CURRENT_USER) {
            return readString(userRoot, hkey, key, valueName);
        } else {
            throw new IllegalArgumentException("hkey=" + hkey);
        }
    }

    /**
     * Read value(s) and value name(s) form given key
     *
     * @param hkey HKEY_CURRENT_USER/HKEY_LOCAL_MACHINE
     * @param key
     * @return the value name(s) plus the value(s)
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    public static Map<String, String> readStringValues(int hkey, String key)
            throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        if (hkey == HKEY_LOCAL_MACHINE) {
            return readStringValues(systemRoot, hkey, key);
        } else if (hkey == HKEY_CURRENT_USER) {
            return readStringValues(userRoot, hkey, key);
        } else {
            throw new IllegalArgumentException("hkey=" + hkey);
        }
    }

    /**
     * Write a value in a given key/value name
     *
     * @param hkey
     * @param key
     * @param valueName
     * @param value
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    public static void writeStringValue
    (int hkey, String key, String valueName, String value)
            throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        if (hkey == HKEY_LOCAL_MACHINE) {
            writeStringValue(systemRoot, hkey, key, valueName, value);
        } else if (hkey == HKEY_CURRENT_USER) {
            writeStringValue(userRoot, hkey, key, valueName, value);
        } else {
            throw new IllegalArgumentException("hkey=" + hkey);
        }
    }

    private static String readString(Preferences root, int hkey, String key, String value)
            throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        int[] handles = (int[]) regOpenKey.invoke(root, hkey, stringToByteArray(key), KEY_READ);

        if (handles[1] != REG_SUCCESS) {
            return null;
        }

        byte[] valb = (byte[]) regQueryValueEx.invoke(root, handles[0], stringToByteArray(value));
        regCloseKey.invoke(root, handles[0]);
        return (valb != null ? new String(valb).trim() : null);
    }

    private static Map<String, String> readStringValues(Preferences root, int hkey, String key)
            throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        HashMap<String, String> results = new HashMap<String, String>();
        int[] handles = (int[]) regOpenKey.invoke(root, hkey, stringToByteArray(key), KEY_READ);
        if (handles[1] != REG_SUCCESS) {
            return null;
        }

        int[] info = (int[]) regQueryInfoKey.invoke(root, handles[0]);

        int count = info[0]; // count
        int maxlen = info[3]; // value length max
        for (int index = 0; index < count; index++) {
            byte[] name = (byte[]) regEnumValue.invoke(root, handles[0], index, maxlen + 1);
            String value = readString(hkey, key, new String(name));
            results.put(new String(name).trim(), value);
        }
        regCloseKey.invoke(root, handles[0]);
        return results;
    }

    private static void writeStringValue(Preferences root, int hkey, String key, String valueName, String value)
            throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        int[] handles = (int[]) regOpenKey.invoke(root, hkey, stringToByteArray(key), KEY_ALL_ACCESS);
        int rc;

        if (handles[ERROR_CODE] == REG_ACCESSDENIED) {
            throw new IllegalAccessException("Key " + key + " cannot be opened. Access denied.\nTry to re-run application with Administrator privileges.");
        } else {
            rc = (Integer) regSetValueEx.invoke(root, handles[0], stringToByteArray(valueName), stringToByteArray(value));
            if (rc == REG_SUCCESS) {
                regCloseKey.invoke(root, handles[0]);
            }
        }
    }

    /**
     * * Returns this java string as a null-terminated byte array
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
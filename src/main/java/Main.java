import java.lang.reflect.InvocationTargetException;

public class Main {
    private static final String LOCK_SCREEN_DEFAULT_LOCATION = "C:\\Windows\\System32\\oobe\\info\\backgrounds\\backgroundDefault.jpg";
    private static final String INPUT_FILE = "C:\\tmp.jpg";

    private static final String REGISTRY_PATH = "SOFTWARE\\Policies\\Microsoft\\Windows\\Personalization";


    // java -jar SetLockScreen-1.0.jar "c:\tmp.jpg"
    public static void main(String... args) {
        String newLocation;
        System.out.println("LockScreenImage = " + lockScreenImageLocation());

        newLocation = args.length > 0 ? args[0] : INPUT_FILE;
        updateLockScreenImageLocation(newLocation);

        System.out.println("New Lock Screen Image = " + lockScreenImageLocation());

    }

    private static void updateLockScreenImageLocation(String newLocation) {

        try {
            WinRegistry.writeStringValue(WinRegistry.HKEY_LOCAL_MACHINE, REGISTRY_PATH, "LockScreenImage", newLocation);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

    }

    private static String lockScreenImageLocation() {
        String value = null;
        try {
            value = WinRegistry.readString(WinRegistry.HKEY_LOCAL_MACHINE, REGISTRY_PATH, "LockScreenImage");
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return value;
    }
}

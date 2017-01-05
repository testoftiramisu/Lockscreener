import java.lang.reflect.InvocationTargetException;

public class Main {
    private static final String INPUT_FILE = "C:\\tmp.jpg";

    private static final String REGISTRY_PATH =
            "SOFTWARE\\Policies\\Microsoft\\Windows\\Personalization";

    /**
     * Application entry point.
     */
    public static void main(String... args) {
        String newLocation;
        System.out.println("Current Lock Screen image: " + lockScreenImageLocation());

        newLocation = args.length > 0 ? args[0] : INPUT_FILE;
        updateLockScreenImageLocation(newLocation);

        System.out.println("New Lock Screen Image = " + lockScreenImageLocation());
    }

    private static void updateLockScreenImageLocation(String newLocation) {
        try {
            System.out.println("Trying to set " + newLocation + " as Lock Screen image...");
            WinRegistry.writeStringValue(WinRegistry.HKEY_LOCAL_MACHINE, REGISTRY_PATH,
                    "LockScreenImage", newLocation);
        } catch (IllegalAccessException ex) {
            System.out.println(ex.getMessage());
            System.exit(1);
        } catch (InvocationTargetException ex) {
            ex.printStackTrace();
        }
    }

    private static String lockScreenImageLocation() {
        String value = null;
        try {
            value = WinRegistry.readString(WinRegistry.HKEY_LOCAL_MACHINE, REGISTRY_PATH,
                    "LockScreenImage");
        } catch (IllegalAccessException ex) {
            ex.printStackTrace();
        } catch (InvocationTargetException ex) {
            ex.printStackTrace();
        }
        return value;
    }
}

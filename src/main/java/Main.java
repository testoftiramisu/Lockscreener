import java.lang.reflect.InvocationTargetException;

public class Main {
    private static final String INPUT_FILE = "C:\\tmp.jpg";

    private static final String REGISTRY_PATH = "SOFTWARE\\Policies\\Microsoft\\Windows\\Personalization";

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
            WinRegistry.writeStringValue(WinRegistry.HKEY_LOCAL_MACHINE, REGISTRY_PATH, "LockScreenImage", newLocation);
        } catch (IllegalAccessException e) {
            System.out.println(e.getMessage());
            System.exit(1);
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

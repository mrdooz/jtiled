package JTiled;

import java.util.Arrays;
import java.util.List;

public class Utils {
    static boolean isValidImageFile(String url) {
        List<String> imgTypes = Arrays.asList(".jpg", ".jpeg", ".png", ".gif", ".bmp");
        return imgTypes.stream().anyMatch(t -> url.endsWith(t));
    }
}

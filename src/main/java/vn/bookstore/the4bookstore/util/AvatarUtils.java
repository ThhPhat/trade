package vn.bookstore.the4bookstore.util;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public final class AvatarUtils {

    private static final String[][] GRADIENTS = {
        {"#1B4D3E", "#2D6A4F"}, // Deep Pine (Forest Folio Primary)
        {"#1A535C", "#2A7B88"}, // Teal Stream
        {"#2C3E50", "#34495E"}, // Midnight Slate
        {"#5C3D75", "#7B4B94"}, // Royal Plum
        {"#8C462E", "#A8583B"}, // Warm Sienna
        {"#2E5B70", "#3A7692"}, // Ocean Navy
        {"#5A6B32", "#6D823D"}, // Olive Moss
        {"#703B4B", "#8B495D"}  // Mulberry Rose
    };

    private AvatarUtils() {
    }

    /**
     * Trích xuất chữ cái đại diện từ tên người dùng.
     * Đối với tên tiếng Việt nhiều từ ("Nguyễn Duy", "Cao Tường", "Nguyễn Minh Thư"):
     * Lấy chữ cái đầu của Tên gọi (từ cuối cùng) -> "D", "T", "T".
     * Đối với tên 1 từ / username ("Admin", "quanly"): Lấy chữ cái đầu tiên -> "A", "Q".
     */
    public static String extractInitial(String name) {
        if (name == null || name.isBlank()) {
            return "U";
        }

        String trimmed = name.trim();
        String[] words = trimmed.split("\\s+");

        // Ưu tiên từ cuối cùng (Tên gọi tiếng Việt)
        String lastWord = words[words.length - 1];
        for (int i = 0; i < lastWord.length(); i++) {
            char c = lastWord.charAt(i);
            if (Character.isLetterOrDigit(c)) {
                return String.valueOf(c).toUpperCase(Locale.forLanguageTag("vi"));
            }
        }

        // Dự phòng: duyệt từ đầu chuỗi
        for (int i = 0; i < trimmed.length(); i++) {
            char c = trimmed.charAt(i);
            if (Character.isLetterOrDigit(c)) {
                return String.valueOf(c).toUpperCase(Locale.forLanguageTag("vi"));
            }
        }

        return "U";
    }

    /**
     * Tự động sinh chuỗi Data URI chứa hình vector SVG icon người màu trắng
     * trên nền xám nhạt tròn phong cách Facebook khi tài khoản chưa có avatar cá nhân.
     */
    public static String generateDefaultAvatarSvg() {
        String svg = "<svg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 100 100' width='100%' height='100%'>"
                + "<rect width='100' height='100' rx='50' fill='#CBD5E1'/>"
                + "<circle cx='50' cy='36' r='18' fill='#FFFFFF'/>"
                + "<path d='M20 84 C20 66, 34 58, 50 58 C66 58, 80 66, 80 84 Z' fill='#FFFFFF'/>"
                + "</svg>";
        return "data:image/svg+xml;base64," + java.util.Base64.getEncoder().encodeToString(svg.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Tự động sinh chuỗi Data URI chứa hình ảnh vector SVG với chữ cái đầu và nền gradient sang trọng.
     */
    public static String generateInitialAvatarSvg(String displayName) {
        String initial = extractInitial(displayName);
        int hash = Math.abs(displayName != null ? displayName.hashCode() : 0);
        String[] colors = GRADIENTS[hash % GRADIENTS.length];

        String svg = "<svg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 100 100' width='100%' height='100%'>"
                + "<defs>"
                + "<linearGradient id='grad' x1='0%' y1='0%' x2='100%' y2='100%'>"
                + "<stop offset='0%' stop-color='" + colors[0] + "'/>"
                + "<stop offset='100%' stop-color='" + colors[1] + "'/>"
                + "</linearGradient>"
                + "</defs>"
                + "<rect width='100' height='100' rx='50' fill='url(#grad)'/>"
                + "<text x='50%' y='53%' font-family='Plus Jakarta Sans, Inter, system-ui, -apple-system, sans-serif' font-size='44' font-weight='700' fill='#FFFFFF' text-anchor='middle' dominant-baseline='central'>"
                + initial
                + "</text>"
                + "</svg>";

        return "data:image/svg+xml;utf8," + URLEncoder.encode(svg, StandardCharsets.UTF_8).replace("+", "%20");
    }

    /**
     * Kiểm tra xem người dùng có ảnh avatar riêng (tải lên hoặc từ Google) hay không.
     */
    public static boolean hasCustomAvatar(String avatarUrl) {
        if (avatarUrl == null || avatarUrl.isBlank()) {
            return false;
        }
        return !avatarUrl.contains("manager-avatar.png") && !avatarUrl.startsWith("data:image/svg+xml");
    }
}

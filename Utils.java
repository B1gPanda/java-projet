public class Utils {

    public static int random(int max) {
        return (int)(Math.random() * max);
    }

    public static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

public class Main {

    private static Random random = new Random();

    public static final String VERIFY_CODES = "123456789ABCDEFGHIJKLMNPQRSTUVWXYZ";
    // ??????????????????????§Õ????????
    private static ImgFontByte imgFontByte = new ImgFontByte();

    private static int[] fontStyle = { Font.PLAIN };

    private static String generateVerifyCode() {

        int codesLen = VERIFY_CODES.length();
        Random rand = new Random(System.currentTimeMillis());
        StringBuilder verifyCode = new StringBuilder(6);
        for (int i = 0; i < 6; i++) {
            verifyCode.append(VERIFY_CODES.charAt(rand.nextInt(codesLen)));
        }

        return verifyCode.toString();
    }

    public static boolean isChineseChar(char c) {
        return String.valueOf(c).matches("[\u4e00-\u9fa5]");
    }

    /**
     * ???????????????
     */
    private static void outputImage(int w, int h, String path, String code, Color needColor, StringBuilder needCode)
            throws IOException {
        Color[] fontColor = new Color[] { needColor, needColor, Color.BLUE, Color.YELLOW, Color.RED, Color.BLACK };
        int verifySize = code.length();
        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = image.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Color c = getRandColor();
        g2.setColor(c);// ???????
        g2.fillRect(0, 0, w, h);
        g2.setColor(c);// ????????
        g2.fillRect(0, 2, w, h);

        Color s = getRandColor();
        char[] charts = code.toCharArray();
        for (int i = 0; i < charts.length; i++) {

            g2.setColor(s);// ????????
            g2.setFont(getRandomFont(h));
            g2.fillRect(0, 6, w, h - 12);
        }
        g2.setColor(Color.GREEN);// ?????????????????
        int lineNumbers = random.nextInt(4);

        for (int i = 0; i < lineNumbers; i++) {
            int x = random.nextInt(w - 1);
            int y = random.nextInt(h - 1);
            int xl = random.nextInt(6) + 1;
            int yl = random.nextInt(12) + 1;
            // g2.drawLine(x, y, x + xl + 40, y + yl + 20);
        }

        // 2.???????
        float yawpRate = 0.05f;
        int area = (int) (yawpRate * w * h);
        for (int i = 0; i < area; i++) {
            int x = random.nextInt(w);
            int y = random.nextInt(h);
            int rgb = getRandomIntColor();
            image.setRGB(x, y, rgb);
        }

        // 3.??????
        shear(g2, w, h, s, c);

        char[] chars = code.toCharArray();

        // ???????
        Random random = new Random();
        Color defaultFontColor;
        int n = random.nextInt(fontColor.length);
        defaultFontColor = fontColor[n];

        int[] ch = { 0, 1, 2, 3, 4, 5 };
        Set<Integer> set = new HashSet<>();
        Random ran = new Random();
        while (set.size() < verifySize) {
            set.add(ch[ran.nextInt(ch.length)]);
        }
        Object[] choice = set.toArray();
        Arrays.sort(choice);
        Random rand = new Random();
        int mustId = ran.nextInt(verifySize);
        int offset = random.nextInt(20);
        for (int i = 0; i < verifySize; i++) {
            int id = (int) choice[i];
            double rd = rand.nextDouble();
            boolean rb = rand.nextBoolean();
            Color tempColor;
            if (i != mustId) {
                tempColor = (Color) randomArray(fontColor);
            } else {
                tempColor = needColor;
            }
            Color need = tempColor;
            if (needCode != null && need.equals(needColor)) {
                needCode.append(chars[i]);
            }
            g2.setColor(need);
            if (isChineseChar(chars[i])) {
                g2.setFont(new Font("????", Font.PLAIN ,getRandomFontSize(h)));

            } else {

                g2.setFont(getRandomFont(h));
            }

            AffineTransform affine = new AffineTransform();
            affine.setToRotation(Math.PI / 4 * rd * (rb ? 1 : -1), (w / verifySize) * i, h / 2);
            g2.setTransform(affine);
            g2.drawChars(chars, i, 1, ((w - 10) / verifySize - 3) * i + 5, h / 2 + (h - 4) / 2 - 8);

        }

        for (int i = 0; i < (random.nextInt(5) + 1); i++) {
            Color tempColor;
            tempColor = (Color) randomArray(new Color[] { defaultFontColor, Color.GREEN });
            g2.setColor(Color.GREEN);
            g2.drawLine(random.nextInt(100), random.nextInt(30), random.nextInt(100), random.nextInt(30));
        }

        g2.dispose();

        File dir = new File(path);
        File outputFile = new File(dir, (needCode == null ? code : needCode.toString()) + "_"
                + UUID.randomUUID().toString().replace("-", "").toLowerCase() + ".png");

        File parentFile = outputFile.getParentFile();
        if (!parentFile.exists()) {
            parentFile.mkdirs();
        }
        outputFile.createNewFile();
        FileOutputStream fos = new FileOutputStream(outputFile);
        ImageIO.write(image, "jpg", fos);
        fos.close();

    }

    private static Object randomArray(Object[] elements) {
        Random random = new Random();
        int n = random.nextInt(elements.length);
        return elements[n];
    }

    private static Color getRandColor() {
        int fc = 100;
        int bc = 250;
        int r = fc + random.nextInt(bc - fc);
        int g = fc + random.nextInt(bc - fc);
        int b = fc + random.nextInt(bc - fc);
        return new Color(r, g, b);
    }

    private static int getRandomIntColor() {
        int[] rgb = getRandomRgb();
        int color = 0;
        for (int c : rgb) {
            color = color << 8;
            color = color | c;
        }
        return color;
    }

    private static int[] getRandomRgb() {
        int[] rgb = new int[3];
        for (int i = 0; i < 3; i++) {
            rgb[i] = random.nextInt(255);
        }
        return rgb;
    }

    /**
     * ??????‰]???????????§³
     */
    private static Font getRandomFont(int h) {
        // ???????
        int style = fontStyle[random.nextInt(fontStyle.length)];
        // ?????§³
        int size = getRandomFontSize(h);

        return new ImgFontByte().getFont(size, style);

    }

    /**
     * ????????§³????¦¶???
     */
    private static int getRandomFontSize(int h) {
        return h - 16;
    }

    /**
     * 3D?§á????????????????
     */
    static class ImgFontByte {
        public Font getFont(int fontSize, int fontStyle) {
            try {
                return new Font("Action Jackson", fontStyle, fontSize);
            } catch (Exception e) {
                return new Font("Arial", fontStyle, fontSize);
            }
        }
    }

    /**
     * ?????????????
     */
    private static void shear(Graphics g, int w1, int h1, Color color, Color c) {
        shearX(g, w1, h1, color);
        shearY(g, w1, h1, color, c);
    }

    /**
     * x?????
     */
    private static void shearX(Graphics g, int w1, int h1, Color color) {
        int period = random.nextInt(2);

        int frames = 1;
        int phase = random.nextInt(2);

        for (int i = 0; i < h1; i++) {
            double d = (double) (period / 2)
                    * Math.sin((double) i / (double) period + (6.2831853071795862D * (double) phase) / (double) frames);

            g.copyArea(0, i, w1, 1, (int) d, 0);
            g.setColor(color);
            g.drawLine((int) d, i, 0, i);
            g.drawLine((int) d + w1, i, w1, i);

        }
    }

    /**
     * y?????
     */
    private static void shearY(Graphics g, int w1, int h1, Color color, Color c) {
        int period = random.nextInt(40) + 10; // 50;
        int frames = 20;
        int phase = 7;
        for (int i = 0; i < w1; i++) {
            double d = (double) (period / 2)
                    * Math.sin((double) i / (double) period + (6.2831853071795862D * (double) phase) / (double) frames);
            g.copyArea(i, 0, 1, h1, 0, (int) d);
            g.setColor(c);
            g.drawLine(i, (int) d, i, 0);
            g.drawLine(i, (int) d + h1, i, h1);
        }
    }

    /**
     * ?????????????????????????????????§¹??
     */
    public static void main(String[] args) throws IOException {
        int w = 90, h = 35;
        GraphicsEnvironment e = GraphicsEnvironment.getLocalGraphicsEnvironment();
        String[] fontName = e.getAvailableFontFamilyNames();

        for (int i = 0; i < fontName.length; i++) {
            System.out.println(fontName[i]);
        }

        for (int i = 0; i < 50000; i++) {
            StringBuilder needCode = new StringBuilder();
            String verifyCode = generateVerifyCode();
            System.out.println("?????" + i + "????");
            outputImage(w, h, "E:/TrainSet/java_captcha__Red", verifyCode, Color.RED, needCode);
        }
        for (int i = 0; i < 50000; i++) {
            StringBuilder needCode = new StringBuilder();
            String verifyCode = generateVerifyCode();
            System.out.println("?????" + i + "????");
            outputImage(w, h, "E:/TrainSet/java_captcha_3_Blue", verifyCode, Color.BLUE, needCode);
        }
        for (int i = 0; i < 50000; i++) {
            StringBuilder needCode = new StringBuilder();
            String verifyCode = generateVerifyCode();
            System.out.println("?????" + i + "????");
            outputImage(w, h, "E:/TrainSet/java_captcha_3_Yellow", verifyCode, Color.YELLOW, needCode);
        }
        for (int i = 0; i < 10000; i++) {
            StringBuilder needCode = new StringBuilder();
            String verifyCode = generateVerifyCode();
            System.out.println("?????" + i + "????");
            outputImage(w, h, "E:/TrainSet/java_captcha_3_Green", verifyCode, Color.GREEN, needCode);
        }
        for (int i = 0; i < 20000; i++) {
            StringBuilder needCode = new StringBuilder();
            String verifyCode = generateVerifyCode();
            System.out.println("?????" + i + "????");
            outputImage(w, h, "E:/TrainSet/java_captcha_3_Black", verifyCode, Color.BLACK, needCode);
        }
        for (int i = 0; i < 100000; i++) {
            String verifyCode = generateVerifyCode();
            System.out.println("?????" + i + "????");
            outputImage(w, h, "E:/TrainSet/AllColor", verifyCode, Color.BLACK, null);
        }

    }
}
package org.example;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.google.zxing.qrcode.encoder.Encoder;
import com.google.zxing.qrcode.encoder.QRCode;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 *
 * @author Stalin
 */
public class QRCodeUtils {

    private static final double PERCENTAGE = 0.2;

    private static final Logger LOG = Logger.getLogger(QRCodeUtils.class.getName());

    public static byte[] generate(String text, int width, int height) throws QRCodeException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);
            MatrixToImageWriter.writeToStream(bitMatrix, "png", baos);
            return baos.toByteArray();
        } catch (WriterException | IOException ex) {
            throw new QRCodeException(QRCodeUtils.class.toString(), ex);
        }
    }

    private static BufferedImage generate(String text) throws QRCodeException {
        try {
            QRCode qrCode = Encoder.encode(text, ErrorCorrectionLevel.H, Collections.singletonMap(EncodeHintType.CHARACTER_SET, "UTF-8"));
            final int qrWidth = qrCode.getMatrix().getWidth();
            final int qrHeight = qrCode.getMatrix().getHeight();

            int borderSizeX2 = (int) (((double) qrWidth) * PERCENTAGE);
            borderSizeX2 = borderSizeX2 % 2 == 0 ? borderSizeX2 : borderSizeX2 + 1;

            final int width = qrWidth + borderSizeX2;
            final int height = qrHeight + borderSizeX2;

            LOG.log(Level.INFO, "Generar QR: version {0}, qrWidth {1}, qrHeight {2}, percentage {3}, borderSizeX2 {4}, width {5}, height {6}"
                    , new Object[]{qrCode.getVersion(), qrWidth, qrHeight, PERCENTAGE, borderSizeX2, width, height});

            final int borderSize = borderSizeX2 / 2;

            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            int[] rgbArray = new int[width * height];
            int i = 0;
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int qrX = x - borderSize;
                    int qrY = y - borderSize;
                    if ((qrX >= 0 && qrX <= (qrWidth - 1)) && (qrY >= 0 && qrY <= (qrHeight - 1))) {
                        rgbArray[i] = qrCode.getMatrix().get(qrX, qrY) > 0 ? 0xFFFFFF : 0x000000;
                    } else {
                        rgbArray[i] = 0xFFFFFF;
                    }
                    i++;
                }
            }
            image.setRGB(0, 0, width, height, rgbArray, 0, width);
            return image;
        } catch (WriterException ex) {
            throw new QRCodeException(QRCodeUtils.class.toString(), ex);
        }
    }

    private static BufferedImage generateWithoutLogo(String text, int width, int height) throws QRCodeException {
        BufferedImage image = generate(text);
        return resizeImage(image, width, height);
    }

    private static BufferedImage getLogo( int width, int height) throws IOException {
        InputStream is = QRCodeUtils.class.getResourceAsStream("/marca.png");
        BufferedImage image = ImageIO.read(is);
        return resizeImage(image, (int) (width * 0.8 * 0.6), (int) (height * 0.8 * 0.144));
    }

    public static byte[] generateWithLogo(String text, int width, int height) throws QRCodeException {
        try {
            BufferedImage qrImage = generateWithoutLogo(text, width, height);

            // Initialize combined image
            BufferedImage combined = new BufferedImage(qrImage.getHeight(), qrImage.getWidth(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics2D = combined.createGraphics();

            // Write QR code to new image at position 0/0
            graphics2D.drawImage(qrImage, 0, 0, null);
            graphics2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));

            BufferedImage overly = getLogo(width, height);

            // Calculate the delta height and width between QR code and logo
            int deltaHeight = qrImage.getHeight() - overly.getHeight();
            int deltaWidth = qrImage.getWidth() - overly.getWidth();

            // Write logo into combine image at position (deltaWidth / 2) and
            // (deltaHeight / 2). Background: Left/Right and Top/Bottom must be
            // the same space for the logo to be centered
            graphics2D.drawImage(overly, (int) Math.round(deltaWidth / 2), (int) Math.round(deltaHeight / 2), null);
            graphics2D.dispose();

            // Write combined image as PNG to OutputStream
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            ImageIO.write(combined, "png", os);

            return os.toByteArray();
        } catch (IOException ex) {
            throw new QRCodeException(QRCodeUtils.class.toString(), ex);
        }
    }

    private static BufferedImage convertToBufferedImage(Image image) {
        if (image == null) {
            throw new IllegalArgumentException();
        }

        if (image instanceof BufferedImage) {
            return (BufferedImage) image;
        }

        // Create a buffered image with transparency
        BufferedImage bufferedImage = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);

        Graphics2D graphics2D = bufferedImage.createGraphics();
        graphics2D.drawImage(image, 0, 0, null);
        graphics2D.dispose();

        return bufferedImage;
    }

    private static BufferedImage resizeImage(BufferedImage image, int newWidth, int newHeight) {
        Image newResizedImage = image.getScaledInstance(newWidth, newHeight, Image.SCALE_AREA_AVERAGING);
        return convertToBufferedImage(newResizedImage);
    }
}

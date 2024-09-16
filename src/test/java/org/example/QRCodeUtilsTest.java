package org.example;

import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author samagua
 */
public class QRCodeUtilsTest {

    @Test
    @SuppressWarnings("UseSpecificCatch")
    public void generate() {
        try {
            byte[] qr = QRCodeUtils.generateWithLogo("https://stackoverflow.com/questions/35104305/how-to-generate-qr-code-with-logo-inside-it/35104430#35104430", 500, 500);
            Logger.getLogger(QRCodeUtilsTest.class.getName()).log(Level.INFO, Base64.getEncoder().encodeToString(qr));
            assertNotNull(qr);
        } catch (Exception ex) {
            Logger.getLogger(QRCodeUtilsTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}

package zm.unza.tels.cemis.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import zm.unza.tels.cemis.entity.Certificate;

import java.io.File;
import java.nio.file.Files;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")                    private String mailFrom;
    @Value("${app.public-url}")                   private String appUrl;
    @Value("${app.cert.pdf-dir:./cert-pdfs}")     private String pdfDir;
    @Value("${app.branding.dir:./branding-assets}") private String brandingDir;

    public void sendCertificateEmail(Certificate cert) throws Exception {
        if (cert.getRecipientEmail() == null || cert.getRecipientEmail().isBlank())
            throw new IllegalArgumentException("Certificate has no recipient email");

        String code       = cert.getCertificateCode() != null ? cert.getCertificateCode() : cert.getCertificateId();
        String verifyUrl  = appUrl + "/verify/" + code;
        String name       = cert.getRecipientName();
        String programme  = cert.getProgramme();

        // Locate the PDF file
        byte[] pdfBytes = null;
        String pdfFilename = code + ".pdf";
        if (cert.getPdfPath() != null) {
            File f = new File(cert.getPdfPath());
            if (f.exists()) pdfBytes = Files.readAllBytes(f.toPath());
        }
        if (pdfBytes == null) {
            File f = new File(pdfDir, pdfFilename);
            if (f.exists()) pdfBytes = Files.readAllBytes(f.toPath());
        }

        MimeMessage msg = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
        helper.setFrom(mailFrom);
        helper.setTo(cert.getRecipientEmail());
        helper.setSubject("Your Certificate — " + programme);
        helper.setText(buildHtml(name, programme, code, verifyUrl), true);

        // Embed logo — classpath static/logo.png (packaged), else branding-assets/seal.png
        try {
            byte[] logoBytes = null;
            var classpathLogo = new ClassPathResource("static/logo.png");
            if (classpathLogo.exists()) {
                logoBytes = classpathLogo.getInputStream().readAllBytes();
            } else {
                java.nio.file.Path sealPath = java.nio.file.Paths.get(brandingDir).toAbsolutePath().resolve("seal.png");
                if (java.nio.file.Files.exists(sealPath))
                    logoBytes = java.nio.file.Files.readAllBytes(sealPath);
            }
            if (logoBytes != null)
                helper.addInline("logo@unza.ac.zm", new ByteArrayResource(logoBytes), "image/png");
        } catch (Exception ignored) {}

        // Attach PDF
        if (pdfBytes != null) {
            helper.addAttachment("Certificate-" + code + ".pdf",
                new ByteArrayResource(pdfBytes), "application/pdf");
        }

        // Deliverability headers
        msg.addHeader("X-Mailer", "UNZA-TeLS-Mailer/1.0");
        msg.addHeader("Precedence", "bulk");
        msg.addHeader("List-Unsubscribe", "<mailto:train@unza.ac.zm?subject=unsubscribe>");
        msg.addHeader("List-Unsubscribe-Post", "List-Unsubscribe=One-Click");

        mailSender.send(msg);
        log.info("[email] Certificate {} sent to {}", code, cert.getRecipientEmail());
    }

    private String buildHtml(String name, String programme, String code, String verifyUrl) {
        return """
            <!DOCTYPE html>
            <html lang="en">
            <head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1"></head>
            <body style="margin:0;padding:0;background:#f5f5f5;font-family:Arial,Helvetica,sans-serif">
              <table width="100%%" cellpadding="0" cellspacing="0" style="background:#f5f5f5;padding:32px 0">
                <tr><td align="center">
                  <table width="600" cellpadding="0" cellspacing="0" style="background:#ffffff;border-radius:8px;overflow:hidden;max-width:600px;width:100%%">
                    <tr>
                      <td style="background:#1a5c2e;padding:28px 40px;text-align:center">
                        <img src="cid:logo@unza.ac.zm" alt="UNZA TeLS" style="display:block;margin:0 auto 16px;max-width:120px;max-height:100px;width:auto;height:auto">
                        <p style="margin:0;color:#c9a44c;font-size:13px;letter-spacing:2px;text-transform:uppercase">University of Zambia</p>
                        <h1 style="margin:6px 0 0;color:#ffffff;font-size:22px;font-weight:700">Technology and E-Learning Support Unit (TeLS)</h1>
                      </td>
                    </tr>
                    <tr>
                      <td style="padding:40px">
                        <p style="margin:0 0 16px;font-size:16px;color:#1a1a1a">Dear <strong>%s</strong>,</p>
                        <p style="margin:0 0 24px;font-size:15px;color:#444;line-height:1.6">
                          Congratulations! Your certificate of completion for <strong>%s</strong> has been issued. Please find your certificate attached to this email.
                        </p>
                        <table width="100%%" cellpadding="0" cellspacing="0" style="margin-bottom:24px">
                          <tr>
                            <td style="background:#edf7f0;border:1px solid #b5d9c4;border-radius:6px;padding:16px 20px">
                              <p style="margin:0 0 4px;font-size:11px;color:#666;text-transform:uppercase;letter-spacing:1px">Certificate code</p>
                              <p style="margin:0;font-size:20px;font-weight:700;color:#1a5c2e;letter-spacing:2px;font-family:monospace">%s</p>
                            </td>
                          </tr>
                        </table>
                        <table cellpadding="0" cellspacing="0" style="margin-bottom:32px">
                          <tr>
                            <td>
                              <a href="%s" style="display:inline-block;background:#edf7f0;color:#1a5c2e;text-decoration:none;font-weight:600;font-size:14px;padding:12px 24px;border-radius:6px;border:1px solid #b5d9c4">
                                Verify certificate
                              </a>
                            </td>
                          </tr>
                        </table>
                        <p style="margin:0;font-size:14px;color:#666;line-height:1.5">
                          Questions? Email <a href="mailto:train@unza.ac.zm" style="color:#1a5c2e">train@unza.ac.zm</a> or call +260 775 606 059.
                        </p>
                      </td>
                    </tr>
                    <tr>
                      <td style="background:#f8f8f8;border-top:1px solid #eee;padding:20px 40px;text-align:center">
                        <p style="margin:0;font-size:12px;color:#999">
                          University of Zambia · Technology and E-Learning Support Unit (TeLS)<br>
                          This is an automated message — please do not reply directly to this email.
                        </p>
                      </td>
                    </tr>
                  </table>
                </td></tr>
              </table>
            </body>
            </html>
            """.formatted(name, programme, code, verifyUrl);
    }
}

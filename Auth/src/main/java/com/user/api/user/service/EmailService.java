package com.user.api.user.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String emailFrom;

    public void sendVerificationCode(String to, String code) {
        String subject = "Tu código de verificación — Vexio Multi Store";
        String html = buildVerificationHtml(code);
        send(to, subject, html);
    }

    public void sendWelcome(String to) {
        String subject = "Bienvenido a Vexio Multi Store — Tu cuenta está lista";
        String html = buildWelcomeHtml();
        send(to, subject, html);
    }

    public void sendPasswordChanged(String to) {
        String subject = "Aviso de seguridad: tu contraseña fue actualizada";
        String html = buildPasswordChangedHtml();
        send(to, subject, html);
    }

    public void sendNewLoginAlert(String to, String ipAddress, String userAgent) {
        try {
            String subject = "Aviso de seguridad: nuevo inicio de sesión detectado";
            String html = buildNewLoginHtml(ipAddress, userAgent);
            send(to, subject, html);
        } catch (Exception e) {
            log.warn("No se pudo enviar alerta de inicio de sesión a {}: {}", to, e.getMessage());
        }
    }

    // ─── HTML builders ───────────────────────────────────────────────────────

    private String buildVerificationHtml(String code) {
        return """
                <!DOCTYPE html>
                <html lang="es">
                <head>
                  <meta charset="UTF-8">
                  <meta name="viewport" content="width=device-width,initial-scale=1">
                  <title>Código de verificación</title>
                </head>
                <body style="margin:0;padding:0;background-color:#ebebeb;font-family:'Helvetica Neue',Helvetica,Arial,sans-serif;">
                  <table width="100%%" cellpadding="0" cellspacing="0" style="background-color:#ebebeb;">
                    <tr><td align="center" style="padding:48px 16px;">

                      <table width="560" cellpadding="0" cellspacing="0"
                             style="max-width:560px;width:100%%;background-color:#ffffff;border-radius:16px;overflow:hidden;box-shadow:0 6px 36px rgba(0,0,0,0.13);">

                        <!-- Accent -->
                        <tr><td style="background-color:#0d0d0d;height:5px;font-size:0;line-height:0;">&nbsp;</td></tr>

                        <!-- Brand header -->
                        <tr>
                          <td style="background-color:#111111;padding:36px 48px;text-align:center;">
                            <p style="margin:0 0 4px;font-size:9px;letter-spacing:8px;color:#666666;text-transform:uppercase;font-weight:600;">Global Multi Store</p>
                            <h1 style="margin:0;font-size:28px;font-weight:900;color:#ffffff;letter-spacing:6px;text-transform:uppercase;line-height:1.2;">Vexio Multi Store</h1>
                          </td>
                        </tr>

                        <!-- Label strip -->
                        <tr>
                          <td style="background-color:#1a1a1a;padding:12px 48px;text-align:center;">
                            <p style="margin:0;font-size:10px;letter-spacing:5px;color:#888888;text-transform:uppercase;font-weight:600;">Verificación de identidad</p>
                          </td>
                        </tr>

                        <!-- Body -->
                        <tr>
                          <td style="padding:52px 56px 44px;text-align:center;">

                            <h2 style="margin:0 0 14px;font-size:22px;font-weight:700;color:#0d0d0d;letter-spacing:-0.3px;">Tu código de acceso</h2>
                            <p style="margin:0 0 40px;font-size:15px;color:#666666;line-height:1.75;max-width:360px;margin-left:auto;margin-right:auto;">
                              Ingresa el siguiente código para continuar.<br>
                              Solo es válido por <strong style="color:#0d0d0d;">5 minutos</strong>.
                            </p>

                            <!-- OTP display -->
                            <table cellpadding="0" cellspacing="0" align="center" style="margin:0 auto 18px;">
                              <tr>
                                <td style="background-color:#0d0d0d;border-radius:14px;padding:24px 52px;text-align:center;">
                                  <span style="font-size:52px;font-weight:900;letter-spacing:20px;color:#ffffff;font-family:'Courier New',Courier,monospace;display:block;line-height:1;">%s</span>
                                </td>
                              </tr>
                            </table>

                            <!-- Timer notice -->
                            <table cellpadding="0" cellspacing="0" align="center" style="margin:0 auto 44px;">
                              <tr>
                                <td style="background-color:#fdf9ee;border:1px solid #e8c94a;border-radius:8px;padding:10px 24px;text-align:center;">
                                  <p style="margin:0;font-size:12px;color:#8a6d00;font-weight:600;letter-spacing:0.3px;">⏱ Expira en 5 minutos · No lo compartas con nadie</p>
                                </td>
                              </tr>
                            </table>

                            <p style="margin:0;font-size:13px;color:#aaaaaa;line-height:1.7;">
                              Si no solicitaste este código, ignora este correo.<br>
                              Tu cuenta permanece completamente segura.
                            </p>

                          </td>
                        </tr>

                        <!-- Divider -->
                        <tr><td style="padding:0 48px;"><table width="100%%" cellpadding="0" cellspacing="0"><tr><td style="height:1px;background-color:#eeeeee;font-size:0;">&nbsp;</td></tr></table></td></tr>

                        <!-- Footer -->
                        <tr>
                          <td style="padding:28px 48px;text-align:center;">
                            <p style="margin:0;font-size:12px;color:#cccccc;line-height:1.9;">
                              © 2025 <strong style="color:#999999;">Vexio Multi Store Urban Clothes</strong>. Todos los derechos reservados.<br>
                              Este es un mensaje automático. Por favor no respondas a este correo.
                            </p>
                          </td>
                        </tr>

                      </table>
                    </td></tr>
                  </table>
                </body>
                </html>
                """.formatted(code);
    }

    private String buildWelcomeHtml() {
        return """
                <!DOCTYPE html>
                <html lang="es">
                <head>
                  <meta charset="UTF-8">
                  <meta name="viewport" content="width=device-width,initial-scale=1">
                  <title>Bienvenido a Vexio Multi Store</title>
                </head>
                <body style="margin:0;padding:0;background-color:#ebebeb;font-family:'Helvetica Neue',Helvetica,Arial,sans-serif;">
                  <table width="100%" cellpadding="0" cellspacing="0" style="background-color:#ebebeb;">
                    <tr><td align="center" style="padding:48px 16px;">

                      <table width="560" cellpadding="0" cellspacing="0"
                             style="max-width:560px;width:100%;background-color:#ffffff;border-radius:16px;overflow:hidden;box-shadow:0 6px 36px rgba(0,0,0,0.13);">

                        <!-- Accent -->
                        <tr><td style="background-color:#0d0d0d;height:5px;font-size:0;line-height:0;">&nbsp;</td></tr>

                        <!-- Brand header -->
                        <tr>
                          <td style="background-color:#111111;padding:36px 48px;text-align:center;">
                            <p style="margin:0 0 4px;font-size:9px;letter-spacing:8px;color:#666666;text-transform:uppercase;font-weight:600;">Urban Clothes</p>
                            <h1 style="margin:0;font-size:28px;font-weight:900;color:#ffffff;letter-spacing:6px;text-transform:uppercase;line-height:1.2;">Vexio Multi Store</h1>
                          </td>
                        </tr>

                        <!-- Verified banner -->
                        <tr>
                          <td style="background-color:#1a1a1a;padding:14px 48px;text-align:center;">
                            <p style="margin:0;font-size:10px;letter-spacing:5px;color:#888888;text-transform:uppercase;font-weight:600;">✦ Cuenta verificada ✦</p>
                          </td>
                        </tr>

                        <!-- Hero body -->
                        <tr>
                          <td style="padding:52px 56px 40px;text-align:center;">

                            <h2 style="margin:0 0 14px;font-size:28px;font-weight:900;color:#0d0d0d;letter-spacing:-0.5px;line-height:1.3;">¡Ya eres parte del crew!</h2>
                            <p style="margin:0 0 44px;font-size:15px;color:#666666;line-height:1.8;max-width:380px;margin-left:auto;margin-right:auto;">
                              Tu cuenta ha sido verificada exitosamente.<br>
                              Explora nuestra colección, encuentra tu estilo y representa.
                            </p>

                            <!-- CTA -->
                            <table cellpadding="0" cellspacing="0" align="center" style="margin:0 auto 48px;">
                              <tr>
                                <td style="background-color:#0d0d0d;border-radius:10px;text-align:center;">
                                  <a href="#" style="display:block;padding:16px 52px;color:#ffffff;font-size:13px;font-weight:700;letter-spacing:3px;text-transform:uppercase;text-decoration:none;">Explorar colección</a>
                                </td>
                              </tr>
                            </table>

                            <!-- Perks grid -->
                            <table cellpadding="0" cellspacing="0" width="100%" style="margin-bottom:8px;border:1px solid #f0f0f0;border-radius:12px;overflow:hidden;">
                              <tr>
                                <td style="padding:18px 20px;border-right:1px solid #f0f0f0;text-align:center;vertical-align:top;width:33%;">
                                  <p style="margin:0 0 6px;font-size:20px;line-height:1;">🛍</p>
                                  <p style="margin:0;font-size:12px;font-weight:700;color:#0d0d0d;letter-spacing:0.2px;">Tienda propia</p>
                                  <p style="margin:4px 0 0;font-size:11px;color:#999999;line-height:1.5;">Abre y gestiona tu store</p>
                                </td>
                                <td style="padding:18px 20px;border-right:1px solid #f0f0f0;text-align:center;vertical-align:top;width:33%;">
                                  <p style="margin:0 0 6px;font-size:20px;line-height:1;">🎽</p>
                                  <p style="margin:0;font-size:12px;font-weight:700;color:#0d0d0d;letter-spacing:0.2px;">Ropa urbana</p>
                                  <p style="margin:4px 0 0;font-size:11px;color:#999999;line-height:1.5;">Colecciones exclusivas</p>
                                </td>
                                <td style="padding:18px 20px;text-align:center;vertical-align:top;width:33%;">
                                  <p style="margin:0 0 6px;font-size:20px;line-height:1;">🔐</p>
                                  <p style="margin:0;font-size:12px;font-weight:700;color:#0d0d0d;letter-spacing:0.2px;">Acceso seguro</p>
                                  <p style="margin:4px 0 0;font-size:11px;color:#999999;line-height:1.5;">Verificación en 2 pasos</p>
                                </td>
                              </tr>
                            </table>

                          </td>
                        </tr>

                        <!-- Divider -->
                        <tr><td style="padding:0 48px;"><table width="100%" cellpadding="0" cellspacing="0"><tr><td style="height:1px;background-color:#eeeeee;font-size:0;">&nbsp;</td></tr></table></td></tr>

                        <!-- Footer -->
                        <tr>
                          <td style="padding:28px 48px;text-align:center;">
                            <p style="margin:0;font-size:12px;color:#cccccc;line-height:1.9;">
                              © 2025 <strong style="color:#999999;">Vexio Multi Store Urban Clothes</strong>. Todos los derechos reservados.<br>
                              Este es un mensaje automático. Por favor no respondas a este correo.
                            </p>
                          </td>
                        </tr>

                      </table>
                    </td></tr>
                  </table>
                </body>
                </html>
                """;
    }

    private String buildPasswordChangedHtml() {
        return """
                <!DOCTYPE html>
                <html lang="es">
                <head>
                  <meta charset="UTF-8">
                  <meta name="viewport" content="width=device-width,initial-scale=1">
                  <title>Aviso de seguridad</title>
                </head>
                <body style="margin:0;padding:0;background-color:#ebebeb;font-family:'Helvetica Neue',Helvetica,Arial,sans-serif;">
                  <table width="100%" cellpadding="0" cellspacing="0" style="background-color:#ebebeb;">
                    <tr><td align="center" style="padding:48px 16px;">

                      <table width="560" cellpadding="0" cellspacing="0"
                             style="max-width:560px;width:100%;background-color:#ffffff;border-radius:16px;overflow:hidden;box-shadow:0 6px 36px rgba(0,0,0,0.13);">

                        <!-- Accent -->
                        <tr><td style="background-color:#0d0d0d;height:5px;font-size:0;line-height:0;">&nbsp;</td></tr>

                        <!-- Brand header -->
                        <tr>
                          <td style="background-color:#111111;padding:36px 48px;text-align:center;">
                            <p style="margin:0 0 4px;font-size:9px;letter-spacing:8px;color:#666666;text-transform:uppercase;font-weight:600;">Urban Clothes</p>
                            <h1 style="margin:0;font-size:28px;font-weight:900;color:#ffffff;letter-spacing:6px;text-transform:uppercase;line-height:1.2;">Vexio Multi Store</h1>
                          </td>
                        </tr>

                        <!-- Security alert banner -->
                        <tr>
                          <td style="background-color:#1c1009;padding:13px 48px;text-align:center;">
                            <p style="margin:0;font-size:10px;letter-spacing:5px;color:#cc7700;text-transform:uppercase;font-weight:700;">⚠ Aviso de seguridad</p>
                          </td>
                        </tr>

                        <!-- Body -->
                        <tr>
                          <td style="padding:52px 56px 40px;text-align:center;">

                            <h2 style="margin:0 0 14px;font-size:22px;font-weight:700;color:#0d0d0d;letter-spacing:-0.3px;">Contraseña actualizada</h2>
                            <p style="margin:0 0 36px;font-size:15px;color:#666666;line-height:1.8;max-width:380px;margin-left:auto;margin-right:auto;">
                              La contraseña de tu cuenta en Vexio Multi Store fue cambiada exitosamente.<br>
                              Si realizaste este cambio, no necesitas hacer nada más.
                            </p>

                            <!-- "All good" box -->
                            <table cellpadding="0" cellspacing="0" width="100%" style="margin-bottom:20px;">
                              <tr>
                                <td style="background-color:#f6fbf6;border:1px solid #b8ddb8;border-radius:10px;padding:18px 24px;text-align:left;">
                                  <p style="margin:0;font-size:14px;color:#2d6a2d;line-height:1.6;">
                                    <strong>✓ Todo en orden</strong><br>
                                    <span style="font-size:13px;color:#4a8a4a;">Tu nueva contraseña está activa. Puedes iniciar sesión normalmente.</span>
                                  </p>
                                </td>
                              </tr>
                            </table>

                            <!-- "Not you?" warning box -->
                            <table cellpadding="0" cellspacing="0" width="100%" style="margin-bottom:40px;">
                              <tr>
                                <td style="background-color:#fff8f0;border:1px solid #f0c080;border-radius:10px;padding:18px 24px;text-align:left;">
                                  <p style="margin:0 0 8px;font-size:14px;font-weight:700;color:#7a4500;">¿No fuiste tú?</p>
                                  <p style="margin:0;font-size:13px;color:#9a5a00;line-height:1.7;">
                                    Si no realizaste este cambio, tu cuenta puede estar comprometida.<br>
                                    Contacta a nuestro equipo de soporte de inmediato y cambia tu contraseña desde un dispositivo seguro.
                                  </p>
                                </td>
                              </tr>
                            </table>

                            <p style="margin:0;font-size:13px;color:#aaaaaa;line-height:1.7;">
                              Por tu seguridad, cierra sesión en todos los dispositivos desconocidos.
                            </p>

                          </td>
                        </tr>

                        <!-- Divider -->
                        <tr><td style="padding:0 48px;"><table width="100%" cellpadding="0" cellspacing="0"><tr><td style="height:1px;background-color:#eeeeee;font-size:0;">&nbsp;</td></tr></table></td></tr>

                        <!-- Footer -->
                        <tr>
                          <td style="padding:28px 48px;text-align:center;">
                            <p style="margin:0;font-size:12px;color:#cccccc;line-height:1.9;">
                              © 2025 <strong style="color:#999999;">Vexio Multi Store Urban Clothes</strong>. Todos los derechos reservados.<br>
                              Este es un mensaje automático. Por favor no respondas a este correo.
                            </p>
                          </td>
                        </tr>

                      </table>
                    </td></tr>
                  </table>
                </body>
                </html>
                """;
    }

    private String buildNewLoginHtml(String ipAddress, String userAgent) {
        String ip = ipAddress != null ? ipAddress : "Desconocida";
        String ua = userAgent != null ? userAgent : "Desconocido";
        return """
                <!DOCTYPE html>
                <html lang="es">
                <head>
                  <meta charset="UTF-8">
                  <meta name="viewport" content="width=device-width,initial-scale=1">
                  <title>Nuevo inicio de sesión</title>
                </head>
                <body style="margin:0;padding:0;background-color:#ebebeb;font-family:'Helvetica Neue',Helvetica,Arial,sans-serif;">
                  <table width="100%%" cellpadding="0" cellspacing="0" style="background-color:#ebebeb;">
                    <tr><td align="center" style="padding:48px 16px;">
                      <table width="560" cellpadding="0" cellspacing="0"
                             style="max-width:560px;width:100%%;background-color:#ffffff;border-radius:16px;overflow:hidden;box-shadow:0 6px 36px rgba(0,0,0,0.13);">
                        <tr><td style="background-color:#0d0d0d;height:5px;font-size:0;line-height:0;">&nbsp;</td></tr>
                        <tr>
                          <td style="background-color:#111111;padding:36px 48px;text-align:center;">
                            <p style="margin:0 0 4px;font-size:9px;letter-spacing:8px;color:#666666;text-transform:uppercase;font-weight:600;">Urban Clothes</p>
                            <h1 style="margin:0;font-size:28px;font-weight:900;color:#ffffff;letter-spacing:6px;text-transform:uppercase;line-height:1.2;">Vexio Multi Store</h1>
                          </td>
                        </tr>
                        <tr>
                          <td style="background-color:#1c1009;padding:13px 48px;text-align:center;">
                            <p style="margin:0;font-size:10px;letter-spacing:5px;color:#cc7700;text-transform:uppercase;font-weight:700;">⚠ Nuevo inicio de sesión</p>
                          </td>
                        </tr>
                        <tr>
                          <td style="padding:52px 56px 40px;text-align:center;">
                            <h2 style="margin:0 0 14px;font-size:22px;font-weight:700;color:#0d0d0d;">Se inició sesión en tu cuenta</h2>
                            <p style="margin:0 0 32px;font-size:15px;color:#666666;line-height:1.8;max-width:380px;margin-left:auto;margin-right:auto;">
                              Detectamos un nuevo inicio de sesión. Si fuiste tú, no necesitas hacer nada.
                            </p>
                            <table cellpadding="0" cellspacing="0" width="100%%" style="margin-bottom:32px;border:1px solid #f0f0f0;border-radius:12px;overflow:hidden;text-align:left;">
                              <tr style="background-color:#f9f9f9;">
                                <td style="padding:14px 20px;font-size:12px;font-weight:700;color:#555555;width:40%%;border-bottom:1px solid #f0f0f0;">IP</td>
                                <td style="padding:14px 20px;font-size:13px;color:#222222;border-bottom:1px solid #f0f0f0;">%s</td>
                              </tr>
                              <tr>
                                <td style="padding:14px 20px;font-size:12px;font-weight:700;color:#555555;">Dispositivo</td>
                                <td style="padding:14px 20px;font-size:12px;color:#444444;word-break:break-all;">%s</td>
                              </tr>
                            </table>
                            <table cellpadding="0" cellspacing="0" width="100%%">
                              <tr>
                                <td style="background-color:#fff8f0;border:1px solid #f0c080;border-radius:10px;padding:18px 24px;text-align:left;">
                                  <p style="margin:0 0 6px;font-size:14px;font-weight:700;color:#7a4500;">¿No fuiste tú?</p>
                                  <p style="margin:0;font-size:13px;color:#9a5a00;line-height:1.7;">
                                    Cambia tu contraseña inmediatamente y cierra todas las sesiones activas desde el panel de seguridad.
                                  </p>
                                </td>
                              </tr>
                            </table>
                          </td>
                        </tr>
                        <tr><td style="padding:0 48px;"><table width="100%%" cellpadding="0" cellspacing="0"><tr><td style="height:1px;background-color:#eeeeee;font-size:0;">&nbsp;</td></tr></table></td></tr>
                        <tr>
                          <td style="padding:28px 48px;text-align:center;">
                            <p style="margin:0;font-size:12px;color:#cccccc;line-height:1.9;">
                              © 2025 <strong style="color:#999999;">Vexio Multi Store Urban Clothes</strong>. Todos los derechos reservados.<br>
                              Este es un mensaje automático. Por favor no respondas a este correo.
                            </p>
                          </td>
                        </tr>
                      </table>
                    </td></tr>
                  </table>
                </body>
                </html>
                """.formatted(ip, ua);
    }

    // ─── Send ─────────────────────────────────────────────────────────────────

    private void send(String to, String subject, String html) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(emailFrom);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(message);
            log.info("Email enviado a: {} | asunto: {}", to, subject);
        } catch (MessagingException e) {
            log.error("Error al enviar email a {} | asunto: {} | {}", to, subject, e.getMessage());
            throw new RuntimeException("Error al enviar el correo electrónico");
        }
    }
}

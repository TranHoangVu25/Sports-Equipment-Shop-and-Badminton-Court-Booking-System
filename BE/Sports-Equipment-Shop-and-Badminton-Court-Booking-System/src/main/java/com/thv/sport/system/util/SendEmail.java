package com.thv.sport.system.util;

import com.thv.sport.system.service.EmailService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Slf4j
@Component
public class SendEmail {
    private final EmailService emailService;

    public void sendEmailRegister(String confirmToken, String email) {
        String link = "http://localhost:8086/api/v1/auth/confirm?token=" + confirmToken;
//        String link = "https://fearsome-ollie-correspondently.ngrok-free.dev/api/v1/auth/confirm?token="
//        + confirmToken;
        String htmlContent =
                "<div style='background:#f4f6f9;padding:40px 0;font-family:Arial,sans-serif;'>"

                        + "<div style='max-width:600px;margin:auto;background:#ffffff;border-radius:10px;"
                        + "overflow:hidden;box-shadow:0 4px 12px rgba(0,0,0,0.08);'>"

                        + "<div style='background:linear-gradient(90deg,#0f2027,#203a43,#2c5364);"
                        + "padding:20px;text-align:center;color:white;'>"
                        + "<h2 style='margin:0;font-size:24px;'>QuangVo Shop</h2>"
                        + "<p style='margin:5px 0 0;font-size:14px;opacity:0.9;'>" +
                        "Sports Equipment & Badminton Court Booking</p>"
                        + "</div>"

                        + "<div style='padding:30px;color:#333;'>"

                        + "<p style='font-size:16px;'>Hello,</p>"

                        + "<p style='font-size:15px;'>Thank you for registering an account at "
                        + "<strong>QuangVo Shop</strong>.</p>"

                        + "<p style='font-size:15px;'>To activate your account, please click the button below:</p>"

                        + "<div style='text-align:center;margin:30px 0;'>"
                        + "<a href='" + link + "' "
                        + "style='background:#ff7a18;color:white;padding:14px 28px;"
                        + "text-decoration:none;border-radius:6px;font-weight:bold;"
                        + "font-size:15px;display:inline-block;'>"
                        + "Confirm Account"
                        + "</a>"
                        + "</div>"

                        + "<p style='font-size:14px;color:#555;'>If you did not create this account," +
                        " you can safely ignore this email.</p>"

                        + "<p style='margin-top:30px;font-size:15px;'>Best regards,<br>"
                        + "<strong>QuangVo Shop Team</strong></p>"

                        + "</div>"

                        + "<div style='background:#f4f6f9;padding:15px;text-align:center;font-size:12px;color:#777;'>"
                        + "This is an automated email, please do not reply."
                        + "</div>"

                        + "</div>"
                        + "</div>";;

        emailService.sendMail(email, "Chiikawa Goods Shop - Account Confirmation", htmlContent);
    }

    public void sendEmailForgotPassword(String confirmToken, String email) {

        String link = "http://localhost:8086/api/v1/auth/confirm-forgot?token=" + confirmToken;
//        String link = "https://fearsome-ollie-correspondently.ngrok-free.dev/api/v1/auth/confirm-forgot?token="
//        + confirmToken;

        String htmlContent =
                "<div style='font-family:Arial, sans-serif; line-height:1.6; padding:20px; color:#333;'>"
                        + "<h2 style='color:#ff6f61;'>Chiikawa Goods Shop</h2>"
                        + "<p>Hello,</p>"
                        + "<p>We received a request to reset your password.</p>"
                        + "<p>Please click the button below to continue resetting your password:</p>"

                        + "<div style='margin:30px 0;'>"
                        + "    <a href='" + link + "' "
                        + "       style='display:inline-block; padding:12px 22px; background-color:#ff6f61; "
                        + "              color:white; text-decoration:none; border-radius:6px; font-weight:bold;'>"
                        + "        Reset Password"
                        + "    </a>"
                        + "</div>"

                        + "<p>If you did not request a password reset, please ignore this email.</p>"
                        + "<p>Best regards,<br><strong>Chiikawa Goods Shop Team</strong></p>"

                        + "<hr style='margin-top:40px; border:none; border-top:1px solid #eee;'/>"
                        + "<p style='font-size:12px; color:#777;'>"
                        + "This is an automated email, please do not reply."
                        + "</p>"
                        + "</div>";

        emailService.sendMail(email, "Chiikawa Goods Shop - Reset Your Password", htmlContent);
    }
}

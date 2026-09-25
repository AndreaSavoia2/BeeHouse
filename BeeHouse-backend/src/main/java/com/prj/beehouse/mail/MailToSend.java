package com.prj.beehouse.mail;

import com.prj.beehouse.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

/**
 * Spring component responsible for generating the HTML content of BeeHouse transactional emails.
 */
@Component
@RequiredArgsConstructor
public class MailToSend {

    private final MailService mailService;

    public Mail registrationMail(User user, String password) {
        String content = buildEmail(
                "Welcome to BeeHouse",
                "Your account is ready",
                """
                        <p style="%s">Hi %s,</p>
                        <p style="%s">Your BeeHouse account has been created. Use these temporary credentials for your first login.</p>
                        %s
                        <p style="%s">Keep this information private and change the password after signing in.</p>
                        """.formatted(
                        paragraphStyle(),
                        escape(user.getName()),
                        paragraphStyle(),
                        credentialsBlock(
                                "Username", escape(user.getUsername()),
                                "Temporary password", escape(password)
                        ),
                        paragraphStyle()
                )
        );
        return mailService.createMail(
                user,
                "BeeHouse - Registration",
                content
        );
    }

    public Mail passwordReset(User user, String password) {
        String content = buildEmail(
                "Password reset",
                "Your new temporary password",
                """
                        <p style="%s">Hi %s,</p>
                        <p style="%s">We generated a new temporary password for your BeeHouse account.</p>
                        %s
                        <p style="%s">For security, change it immediately after logging in.</p>
                        """.formatted(
                        paragraphStyle(),
                        escape(user.getName()),
                        paragraphStyle(),
                        singleValueBlock("Temporary password", escape(password)),
                        paragraphStyle()
                )
        );
        return mailService.createMail(
                user,
                "BeeHouse - Password reset",
                content
        );
    }

    public Mail usernameRemind(User user) {
        String content = buildEmail(
                "Username reminder",
                "Here is your BeeHouse username",
                """
                        <p style="%s">Hi %s,</p>
                        <p style="%s">We received a request to remind you of the username linked to this email address.</p>
                        %s
                        <p style="%s">Use it with your password to access BeeHouse.</p>
                        """.formatted(
                        paragraphStyle(),
                        escape(user.getName()),
                        paragraphStyle(),
                        singleValueBlock("Username", escape(user.getUsername())),
                        paragraphStyle()
                )
        );
        return mailService.createMail(
                user,
                "BeeHouse - Username reminder",
                content
        );
    }

    private String buildEmail(String title, String subtitle, String bodyContent) {
        return """
                <!DOCTYPE html>
                <html lang="en">
                    <head>
                        <meta charset="UTF-8"/>
                        <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
                        <title>%s</title>
                    </head>
                    <body style="margin:0; padding:0; background-color:#f3f6f4; font-family:Arial, Helvetica, sans-serif; color:#1f2a24;">
                        <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="background-color:#f3f6f4; padding:32px 16px;">
                            <tr>
                                <td align="center">
                                    <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="max-width:620px; background-color:#ffffff; border:1px solid #dfe7e2; border-radius:12px; overflow:hidden;">
                                        <tr>
                                            <td style="background-color:#f2b705; padding:24px 32px;">
                                                <div style="font-size:14px; line-height:20px; font-weight:700; letter-spacing:0; color:#1f2a24;">BeeHouse</div>
                                                <h1 style="margin:10px 0 0; font-size:26px; line-height:34px; font-weight:700; color:#1f2a24;">%s</h1>
                                                <p style="margin:8px 0 0; font-size:15px; line-height:22px; color:#3f4d45;">%s</p>
                                            </td>
                                        </tr>
                                        <tr>
                                            <td style="padding:30px 32px 10px;">
                                                %s
                                            </td>
                                        </tr>
                                        <tr>
                                            <td style="padding:20px 32px 32px;">
                                                <div style="height:1px; background-color:#e6eee9; margin-bottom:18px;"></div>
                                                <p style="margin:0; font-size:13px; line-height:20px; color:#66736b;">This is an automatic email from BeeHouse. If you did not request this message, contact the administrator.</p>
                                            </td>
                                        </tr>
                                    </table>
                                </td>
                            </tr>
                        </table>
                    </body>
                </html>
                """.formatted(escape(title), escape(title), escape(subtitle), bodyContent);
    }

    private String credentialsBlock(String firstLabel, String firstValue, String secondLabel, String secondValue) {
        return """
                <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="margin:22px 0; border:1px solid #dfe7e2; border-radius:10px; overflow:hidden;">
                    %s
                    %s
                </table>
                """.formatted(valueRow(firstLabel, firstValue), valueRow(secondLabel, secondValue));
    }

    private String singleValueBlock(String label, String value) {
        return """
                <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="margin:22px 0; border:1px solid #dfe7e2; border-radius:10px; overflow:hidden;">
                    %s
                </table>
                """.formatted(valueRow(label, value));
    }

    private String valueRow(String label, String value) {
        return """
                <tr>
                    <td style="padding:16px 18px; background-color:#fbfcfb; border-bottom:1px solid #edf2ef;">
                        <div style="font-size:12px; line-height:18px; color:#66736b; text-transform:uppercase; font-weight:700;">%s</div>
                        <div style="margin-top:4px; font-size:18px; line-height:26px; color:#1f2a24; font-weight:700; word-break:break-word;">%s</div>
                    </td>
                </tr>
                """.formatted(escape(label), value);
    }

    private String paragraphStyle() {
        return "margin:0 0 16px; font-size:15px; line-height:24px; color:#38463e;";
    }

    private String escape(String value) {
        return HtmlUtils.htmlEscape(value == null ? "" : value);
    }
}

package com.bitstrat.utils;

import java.time.LocalDate;

public class EmailTemplateUtil {

    /**
     * 根据语言生成 HTML 邮件模板
     *
     * @param code 验证码
     * @param language 语言，例如 "en", "zh", "ko"
     * @return HTML 邮件字符串
     */
    public static String getVerificationEmailTemplate(String code, String language) {
        String title, greeting, message, validity, ignoreMsg, footer;
        String year = LocalDate.now().getYear() + "";
        switch (language.toLowerCase()) {
            case "zh":
                title = "欢迎注册 BitStrat！";
                greeting = "感谢您注册，请使用以下验证码完成注册：";
                message = "验证码：";
                validity = "此验证码 10 分钟内有效，请勿泄露给他人。";
                ignoreMsg = "如果您未请求此验证码，请忽略本邮件。";
                footer = "&copy; "+year+" BitStrat.org 版权所有";
                break;
            case "ko":
                title = "BitStrat에 오신 것을 환영합니다!";
                greeting = "가입해 주셔서 감사합니다. 다음 인증 코드를 사용하여 가입을 완료하세요:";
                message = "인증 코드:";
                validity = "이 코드는 10분 동안 유효합니다. 다른 사람과 공유하지 마세요.";
                ignoreMsg = "요청하지 않았다면 이 이메일을 무시하세요.";
                footer = "&copy; "+year+" BitStrat.org. 모든 권리 보유.";
                break;
            case "en":
            default:
                title = "Welcome to BitStrat!";
                greeting = "Thank you for registering. Please use the verification code below to complete your registration:";
                message = "Verification Code:";
                validity = "This code is valid for 10 minutes. Do not share it with anyone.";
                ignoreMsg = "If you did not request this, please ignore this email.";
                footer = "&copy; "+year+" BitStrat.org. All rights reserved.";
                break;
        }

        return "<!DOCTYPE html>" +
            "<html lang=\"" + language + "\">" +
            "<head>" +
            "<meta charset=\"UTF-8\">" +
            "<title>" + title + "</title>" +
            "<style>" +
            "body {font-family: Arial, sans-serif; background-color: #f4f4f7; color: #333333; margin: 0; padding: 0;}" +
            ".container {width: 100%; max-width: 600px; margin: 40px auto; background-color: #ffffff; border-radius: 8px; padding: 30px; box-shadow: 0 2px 6px rgba(0,0,0,0.1);}" +
            "h1 {color: #2c3e50; font-size: 24px; margin-bottom: 20px;}" +
            "p {font-size: 16px; line-height: 1.5;}" +
            ".code {display: inline-block; font-size: 28px; letter-spacing: 4px; padding: 12px 20px; background-color: #f1f1f1; border-radius: 6px; margin: 20px 0; font-weight: bold;}" +
            ".footer {font-size: 12px; color: #999999; margin-top: 30px; text-align: center;}" +
            "</style>" +
            "</head>" +
            "<body>" +
            "<div class=\"container\">" +
            "<h1>" + title + "</h1>" +
            "<p>" + greeting + "</p>" +
            "<div class=\"code\">"+ code + "</div>" +
            "<p>" + validity + "</p>" +
            "<p>" + ignoreMsg + "</p>" +
            "<div class=\"footer\">" + footer + "</div>" +
            "</div>" +
            "</body>" +
            "</html>";
    }
}

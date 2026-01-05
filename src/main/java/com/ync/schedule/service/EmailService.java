package com.ync.schedule.service;

import com.ync.schedule.domain.EmailTemplate;
import com.ync.schedule.mapper.EmailTemplateMapper;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender defaultMailSender;
    private final EmailTemplateMapper emailTemplateMapper;

    @Value("${spring.mail.host}")
    private String mailHost;

    @Value("${spring.mail.port}")
    private int mailPort;

    public EmailService(JavaMailSender mailSender, EmailTemplateMapper emailTemplateMapper) {
        this.defaultMailSender = mailSender;
        this.emailTemplateMapper = emailTemplateMapper;
    }

    /**
     * 연차 신청 메일 발송
     */
    public void sendLeaveApplicationEmail(String fromEmail, String smtpPassword, String toEmail, String memberName,
                                         String memberEmailAddr, String department, String position,
                                         String division, String phone,
                                         String leaveType, LocalDate startDate,
                                         LocalDate endDate, String leaveAmount, String reason) {
        try {
            // 사용일수가 1일일 때는 LEAVE_APPLICATION2 템플릿 사용
            String templateName = "1일".equals(leaveAmount) ? "LEAVE_APPLICATION2" : "LEAVE_APPLICATION";

            EmailTemplate template = emailTemplateMapper.findByTemplateName(templateName);
            if (template == null || !template.getIsActive()) {
                log.warn("{} 템플릿을 찾을 수 없거나 비활성화되어 있습니다.", templateName);
                return;
            }

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일");

            Map<String, String> variables = new HashMap<>();
            variables.put("memberName", memberName);
            variables.put("memberEmail", memberEmailAddr != null ? memberEmailAddr : "");
            variables.put("department", department != null ? department : "");
            variables.put("position", position != null ? position : "");
            variables.put("division", division != null ? division : "");
            variables.put("phone", phone != null ? phone : "");
            variables.put("leaveType", leaveType);
            variables.put("startDate", startDate.format(formatter));
            variables.put("endDate", endDate.format(formatter));
            variables.put("leaveAmount", leaveAmount);
            variables.put("reason", reason != null && !reason.isEmpty() ? reason : "사유 없음");

            String subject = replaceVariables(template.getSubject(), variables);
            String content = replaceVariables(template.getContent(), variables);

            sendEmail(fromEmail, smtpPassword, toEmail, subject, content);
            log.info("연차 신청 메일 발송 완료: {} -> {}", memberName, toEmail);

        } catch (Exception e) {
            log.error("연차 신청 메일 발송 실패: {}", e.getMessage(), e);
            throw new RuntimeException("메일 발송에 실패했습니다.", e);
        }
    }

    /**
     * 연차 승인 메일 발송
     */
    public void sendLeaveApprovalEmail(String fromEmail, String smtpPassword, String toEmail, String memberName,
                                      String leaveType, LocalDate startDate,
                                      LocalDate endDate, String approverName) {
        try {
            EmailTemplate template = emailTemplateMapper.findByTemplateName("LEAVE_APPROVAL");
            if (template == null || !template.getIsActive()) {
                log.warn("LEAVE_APPROVAL 템플릿을 찾을 수 없거나 비활성화되어 있습니다.");
                return;
            }

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일");

            Map<String, String> variables = new HashMap<>();
            variables.put("memberName", memberName);
            variables.put("leaveType", leaveType);
            variables.put("startDate", startDate.format(formatter));
            variables.put("endDate", endDate.format(formatter));
            variables.put("approverName", approverName);

            String subject = replaceVariables(template.getSubject(), variables);
            String content = replaceVariables(template.getContent(), variables);

            sendEmail(fromEmail, smtpPassword, toEmail, subject, content);
            log.info("연차 승인 메일 발송 완료: {} -> {}", memberName, toEmail);

        } catch (Exception e) {
            log.error("연차 승인 메일 발송 실패: {}", e.getMessage(), e);
            throw new RuntimeException("메일 발송에 실패했습니다.", e);
        }
    }

    /**
     * 연차 반려 메일 발송
     */
    public void sendLeaveRejectionEmail(String fromEmail, String smtpPassword, String toEmail, String memberName,
                                       String leaveType, LocalDate startDate,
                                       LocalDate endDate, String approverName,
                                       String reason) {
        try {
            EmailTemplate template = emailTemplateMapper.findByTemplateName("LEAVE_REJECTION");
            if (template == null || !template.getIsActive()) {
                log.warn("LEAVE_REJECTION 템플릿을 찾을 수 없거나 비활성화되어 있습니다.");
                return;
            }

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일");

            Map<String, String> variables = new HashMap<>();
            variables.put("memberName", memberName);
            variables.put("leaveType", leaveType);
            variables.put("startDate", startDate.format(formatter));
            variables.put("endDate", endDate.format(formatter));
            variables.put("approverName", approverName);
            variables.put("reason", reason != null ? reason : "사유 없음");

            String subject = replaceVariables(template.getSubject(), variables);
            String content = replaceVariables(template.getContent(), variables);

            sendEmail(fromEmail, smtpPassword, toEmail, subject, content);
            log.info("연차 반려 메일 발송 완료: {} -> {}", memberName, toEmail);

        } catch (Exception e) {
            log.error("연차 반려 메일 발송 실패: {}", e.getMessage(), e);
            throw new RuntimeException("메일 발송에 실패했습니다.", e);
        }
    }

    /**
     * 템플릿 변수 치환
     */
    private String replaceVariables(String template, Map<String, String> variables) {
        String result = template;
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            result = result.replace("${" + entry.getKey() + "}", entry.getValue());
        }
        return result;
    }

    /**
     * 메일 발송 - 사용자별 SMTP 설정 사용
     */
    private void sendEmail(String fromEmail, String smtpPassword, String toEmail, String subject, String content) throws MessagingException {
        JavaMailSender mailSender;

        // DEBUG: SMTP 설정 확인
        System.out.println("=== EmailService.sendEmail ===");
        System.out.println("fromEmail: " + fromEmail);
        System.out.println("smtpPassword: " + (smtpPassword != null ? "있음 (길이: " + smtpPassword.length() + ")" : "NULL"));
        System.out.println("toEmail: " + toEmail);

        // SMTP 비밀번호가 있으면 사용자별 SMTP 설정 사용, 없으면 기본 설정 사용
        if (smtpPassword != null && !smtpPassword.isEmpty()) {
            System.out.println(">> 사용자별 SMTP 사용: " + fromEmail);
            mailSender = createMailSender(fromEmail, smtpPassword);
        } else {
            System.out.println(">> 기본 SMTP 사용 (moods2)");
            mailSender = defaultMailSender;
            // 기본 설정 사용 시 moods2@yncsmart.com으로 발송
            fromEmail = "moods2@yncsmart.com";
        }

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(toEmail);
        helper.setSubject(subject);
        helper.setText(content, true);

        mailSender.send(message);
    }

    /**
     * 사용자별 JavaMailSender 생성
     */
    private JavaMailSender createMailSender(String username, String password) {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(mailHost);
        mailSender.setPort(mailPort);
        mailSender.setUsername(username);
        mailSender.setPassword(password);

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");
        props.put("mail.smtp.ssl.enable", "true");
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.socketFactory.fallback", "false");
        props.put("mail.smtp.socketFactory.port", String.valueOf(mailPort));

        return mailSender;
    }
}

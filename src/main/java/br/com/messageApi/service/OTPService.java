package br.com.messageApi.service;

import br.com.messageApi.config.EmailTemplate;
import br.com.messageApi.config.SecurityConfig;
import br.com.messageApi.config.SendEmail;
import br.com.messageApi.dto.ResponseOtpDTO;
import io.jsonwebtoken.JwtException;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class OTPService {

    private final SendEmail sendEmail;

	private final EmailTemplate template;

    public OTPService(SendEmail sendEmail, EmailTemplate template) {
        this.sendEmail = sendEmail;
        this.template = template;
    }

    public Map<String, String> generateOtp(String email) {
        // Generate random 6-digit OTP
        String otp = String.format("%06d", new Random().nextInt(999999));

		Map<String, String> response = new HashMap<>();

		response.put("otp", otp);
		response.put("token", SecurityConfig.createToken(email, otp));

		return response;

    }

	public Map<String, Object> validateOtp(String token, String userEnteredOtp) {

		Map<String, Object> response = new HashMap<>();

		// First check if token is valid (not expired and properly signed)
		if (!SecurityConfig.isTokenValid(token)) {
			response.put("valid", false);
			response.put("error", "Invalid or expired token");
			return response;
		}

		try {
			Map<String, Object> claims = SecurityConfig.parseToken(token);

			String storedOtp = (String) claims.get("otp");
			boolean isOtpValid = storedOtp.equals(userEnteredOtp);

			response.put("valid", isOtpValid);
			response.put("email", claims.get("email"));
		} catch (JwtException e) {
			response.put("valid", false);
			response.put("error", "Invalid or expired token");
		}

		return response;
	}

    public ResponseOtpDTO sendEmail(String email) throws MessagingException {

		Map<String, String> token = generateOtp(email);

		var emailTemplate = template.emailTemplateBuilder(email, token.get("otp"));

        sendEmail.send(email, emailTemplate);

        return new ResponseOtpDTO(token.get("token"), token.get("otp"), "Email enviado com sucesso!");
    }


}

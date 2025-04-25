package br.com.messageApi.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;

@Configuration
public class EmailTemplate {

    public String emailTemplateBuilder(String email, String token){

        return """
            otp.email.template=<!DOCTYPE html>
            <html lang="pt-BR">
                    <head>
                    <meta charset="UTF-8">
                        <title>Redefinição de Senha</title>
                        <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .btn {
                            display: inline-block;
                            padding: 10px 20px;
                            background-color: #28a745;
                            color: #fff;
                            text-decoration: none;
                            border-radius: 4px;
                        }
                    </style>
                    </head>
                    <body>
                    <h2>Olá %s,</h2>
                        <p>Recebemos uma solicitação para redefinir sua senha.</p>
                        <p>este é o Token para habilitar a escolher uma nova senha:</p>
                    <h3><b>%s</b></h3>
                        <p>Se você não solicitou essa alteração, pode ignorar este e‑mail sem problemas.</p>
                    <br/>
                    <p>Atenciosamente,<br/>Equipe de Suporte</p>
                    </body>
                    </html>
        """.formatted(email, token);

    }

}

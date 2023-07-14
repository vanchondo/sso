package com.vanchondo.sso.aspect;

import com.vanchondo.sso.configs.properties.CaptchaConfiguration;
import com.vanchondo.sso.dtos.security.CaptchaDTO;
import com.vanchondo.sso.services.CaptchaValidatorService;
import com.vanchondo.sso.utilities.NetworkUtil;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.jasypt.encryption.StringEncryptor;
import org.jasypt.exceptions.EncryptionOperationNotPossibleException;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;

import javax.servlet.http.HttpServletRequest;

import java.util.Optional;

@Aspect
@Log4j2
@Component
@AllArgsConstructor
public class ValidateCaptchaAspect {

    private final CaptchaValidatorService captchaValidatorService;
    private final StringEncryptor encryptor;
    private final CaptchaConfiguration captchaConfiguration;

    @Around("@annotation(ValidateCaptcha)")
    public Object CheckSecretHeader(ProceedingJoinPoint pjp) throws Throwable {
        MethodSignature signature  = (MethodSignature)pjp.getSignature();
        CaptchaDTO dto = Optional.ofNullable((CaptchaDTO)pjp.getArgs()[0]).orElse(new CaptchaDTO());

        validateCaptcha(dto.getCaptchaResponse());

        return pjp.proceed();
    }

    public void validateCaptcha(String captchaResponse) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String response = null;
        try {
            response = encryptor.decrypt(captchaResponse);
        } catch (EncryptionOperationNotPossibleException ex) {
            log.info("::validateCaptcha::captcha response is not encrypted.");
        }

        if (!captchaConfiguration.getSecret().equals(response)){
            // Validate captcha response, if is invalid, it throws a BadRequest
            captchaValidatorService.validateCaptcha(captchaResponse, NetworkUtil.getClientIp(request));
        }

    }
}

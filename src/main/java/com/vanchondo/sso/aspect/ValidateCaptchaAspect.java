package com.vanchondo.sso.aspect;

import com.vanchondo.sso.dtos.security.CaptchaDTO;
import com.vanchondo.sso.services.CaptchaValidatorService;
import com.vanchondo.sso.utilities.NetworkUtil;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.jasypt.encryption.StringEncryptor;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import lombok.AllArgsConstructor;

import javax.servlet.http.HttpServletRequest;

import java.util.Optional;

@Aspect
@Component
@AllArgsConstructor
public class ValidateCaptchaAspect {

    private final CaptchaValidatorService captchaValidatorService;
    private final StringEncryptor encryptor;

    @Around("@annotation(ValidateCaptcha)")
    public Object CheckSecretHeader(ProceedingJoinPoint pjp) throws Throwable {
        MethodSignature signature  = (MethodSignature)pjp.getSignature();
        CaptchaDTO dto = Optional.ofNullable((CaptchaDTO)pjp.getArgs()[0]).orElse(new CaptchaDTO());
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();

        // Validate captcha response, if is invalid, it throws a BadRequest
        captchaValidatorService.validateCaptcha(dto.getCaptchaResponse(), NetworkUtil.getClientIp(request));

        return pjp.proceed();
    }
}

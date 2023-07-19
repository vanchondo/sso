package com.vanchondo.sso.aspect;

import com.vanchondo.sso.configs.properties.CaptchaConfiguration;
import com.vanchondo.sso.dtos.security.CaptchaDTO;
import com.vanchondo.sso.services.CaptchaValidatorService;
import com.vanchondo.sso.utilities.NetworkUtil;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
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
    private final CaptchaConfiguration captchaConfiguration;

    @Around("@annotation(ValidateCaptcha)")
    public Object validateCaptcha(ProceedingJoinPoint pjp) throws Throwable {
        String methodName = "::validateCaptcha::";
        log.info("{}Entering validating captcha annotation", methodName);
        CaptchaDTO dto = Optional.ofNullable((CaptchaDTO)pjp.getArgs()[0]).orElse(new CaptchaDTO());

        validateCaptcha(dto);

        log.info("{}Captcha validation was successful, continue with the endpoint", methodName);
        return pjp.proceed();
    }

    private void validateCaptcha(CaptchaDTO dto) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();

        String methodName = "::validateCaptcha::";
        if (captchaConfiguration.getSecret().equals(dto.getCaptchaResponse())){
            log.info("{}This request is for testing purposes, captcha secret provided", methodName);
            dto.setTest(true);
        }
        else {
            // Validate captcha response, if is invalid, it throws a BadRequest
            captchaValidatorService.validateCaptcha(dto.getCaptchaResponse(), NetworkUtil.getClientIp(request));
        }

    }
}

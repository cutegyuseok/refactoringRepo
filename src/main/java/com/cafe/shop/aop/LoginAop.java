package com.cafe.shop.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpSession;

@Component
@Aspect
public class LoginAop {

    @Pointcut("execution(* com.cafe.shop.member.controller.MemberController.*(..))" +
            "&& @annotation(com.cafe.shop.aop.Login)")
    public void cut(){};


    @Around("cut()")
    public Object checkLogin(ProceedingJoinPoint joinPoint)throws Throwable{
        HttpSession session = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest().getSession();
        if(session.getAttribute("id")==null){
            return "loginFail";
        }else {
            return joinPoint.proceed();
        }
    }
}

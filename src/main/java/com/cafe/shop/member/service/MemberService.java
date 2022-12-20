package com.cafe.shop.member.service;

import com.cafe.shop.mapper.MemberMapper;
import com.cafe.shop.member.dto.Member;
import com.cafe.shop.member.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.sql.SQLIntegrityConstraintViolationException;

@Service
public class MemberService {

    @Autowired
    MemberRepository repo;

    public int signup(Member member){
        int result=0;
        try{
            result=repo.signup(member);
        }catch(Exception e){
            result=-1;
        }

        return result;
    }


    public Member login(Member member){
        return repo.login(member);

    }

}

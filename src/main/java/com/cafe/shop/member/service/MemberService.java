package com.cafe.shop.member.service;

import com.cafe.shop.mapper.MemberMapper;
import com.cafe.shop.member.dto.Member;
import com.cafe.shop.member.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

    public List<HashMap<String,Object>> selectUserCart(String id){
        return repo.selectUserCart(id);
    }
    public List<HashMap<String,Object>>  selectCart(String id){
        return repo.selectCart(id);

    }
    public int updateCartAmount(Map<String,String> param){
        return repo.updateCartAmount(param);
    }
    public int insertCart(Map<String,String> param){
        return repo.insertCart(param);

    }
    public int selectAvailAmount(Map<String,String> param){
        return repo.selectAvailAmount(param);
    }
    public String  deleteCart(Map<String,String> param){
        if (repo.deleteCart(param) == 1) {
            return "success";
        } else {
            return "failed";
        }
    }
    public int deleteCartAll(String param){
        return repo.deleteCartAll(param);
    }
    public int updateAmountProduct(Map<String, String> param) {
        return repo.updateAmountProduct(param);
    }

    public int insertOrderBook(Map<String, String> param) {
        return repo.insertOrderBook(param);
    }

    public int insertOrderDetail(Map<String, String> param) {
        return repo.insertOrderDetail(param);
    }

    public List<HashMap<String, Object>> selectOrderBook(String id) {
        return repo.selectOrderBook(id);

    }
    public List<HashMap<String, Object>> selectOrderDetail(HashMap<String, String> params) {
        return repo.selectOrderDetail(params);
    }

    public boolean addTempCart(Object pocket, String id,List<Map<String, String>> putList){
        Map<String, List<LinkedHashMap<String, String>>> param = (Map<String, List<LinkedHashMap<String, String>>>) pocket;
        List<LinkedHashMap<String, String>> cartList = param.get("pocket");
        List<HashMap<String, Object>> cartSaved = selectCart(id);
        for (LinkedHashMap<String, String> one : cartList) {
            String flag = "false";
            int amount = 0;
            String cate = "";
            for (int i = 0; i < cartSaved.size(); i++) {
                if (String.valueOf(cartSaved.get(i).get("productid")).equals(String.valueOf(one.get("id")))) {
                    flag = "true";
                    amount = Integer.parseInt(String.valueOf(cartSaved.get(i).get("amount")));
                    cate = ((HashMap<String, Object>)cartSaved.get(i)).get("catename").toString();
                }
            }
            int added = Integer.parseInt(String.valueOf(one.get("amount")));
            Map<String, String> daoMap = new HashMap<>();
            daoMap.put("productid", String.valueOf(one.get("id")));
            daoMap.put("amount", added + "");
            daoMap.put("userid", id);
            daoMap.put("flag", flag);

            if (flag.equals("false")) {
                cate = one.get("cate");
            }
            daoMap.put("cate", cate);

            if (cate.equals("쿠키")) {
                if (added > 5) {
                    return false;
                }
            }
            putList.add(daoMap);
        }
        return true;
    }

    public boolean checkMaxAmount(List<Map<String, String>> putList){
        int total =0;
        for (Map<String, String> cart : putList) {
            if (cart.get("cate").equals("쿠키"))
                total += Integer.parseInt(cart.get("amount"));
        }
        if (total > 14) {
            return false;
        }
        return true;
    }
}

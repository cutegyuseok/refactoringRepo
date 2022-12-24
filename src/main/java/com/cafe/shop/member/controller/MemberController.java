package com.cafe.shop.member.controller;

import com.cafe.shop.aop.Login;
import com.cafe.shop.member.dto.Member;
import com.cafe.shop.member.service.MemberService;
import com.cafe.shop.product.service.ProductService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.*;

@RestController
public class MemberController {

    @Autowired
    MemberService ms;
    @Autowired
    ProductService ps;

    @GetMapping("/selectUserCart")
    public List<HashMap<String,Object>> selectUserCart(HttpSession session){
        String id=String.valueOf(session.getAttribute("id"));
        return ms.selectUserCart(id);
    }

    @GetMapping("/selectCart")
    public List<HashMap<String,Object>>  selectCart(HttpSession session){
        String id=String.valueOf(((Member)session.getAttribute("id")).getId());
        return ms.selectCart(id);
    }

    @Login
    @RequestMapping("/addCart")
    public @ResponseBody String addCart(@RequestBody(required = false) Object pocket, HttpSession session) {
        List<Map<String, String>> putList = new ArrayList<Map<String, String>>();
        if(!ms.addTempCart(pocket,String.valueOf(((Member)session.getAttribute("id")).getId()),putList)
                || !ms.checkMaxAmount(putList))return "amount";
        return ms.cartVariation(putList);
    }

    @Login
    @PostMapping("/deleteCart")
    public String deleteCart(String id, HttpSession session) {
        Map<String, String> accParam = new HashMap<>();
        accParam.put("userid", ((Member) session.getAttribute("id")).getId());
        accParam.put("id", id);
        return ms.deleteCart(accParam);
    }
    @PostMapping("/selectAvailAmountCart")
    public String selectAvailAmountCart(String id) {
        Map<String, String> data = new HashMap<String, String>();
        data.put("id", id);
        int result = ms.selectAvailAmount(data);
        return result + "";
    }

    @Login
    @PostMapping("/confirmBuying")
    public  String confirmBuying(@RequestBody(required = false) Object pocket, HttpSession session) {
        String userid=((Member) session.getAttribute("id")).getId();
        List<HashMap<String, Object>> idList=ps.checkAvailProductId();
        return ms.confirmBuying(userid,pocket,idList);
    }

    @Login
    @PostMapping("/confirmRest")
    public Object confirmRest(@RequestBody(required = false) Object pocket, HttpSession session) {

        String userid = null;
        Member m=(Member) session.getAttribute("id");

        Map<String, String> accParam = new HashMap<>();
        userid=m.getId();


        Map<String, List<LinkedHashMap<String, String>>> param = (Map<String, List<LinkedHashMap<String, String>>>) pocket;

        LinkedHashMap<String, String> user = new LinkedHashMap<>();
        List<LinkedHashMap<String, String>> cart = param.get("pocket");

        user.put("name",m.getName());
        user.put("phone",m.getPhone());
        user.put("postcode",m.getPostcode());
        user.put("address",m.getAddress());
        user.put("addressDetail",m.getAddressDetail());


        boolean errorFlag=false;

        for (LinkedHashMap<String, String> ss : cart) {
            HashMap<String, String> ckcParam = new HashMap<>();
            ckcParam.put("id", String.valueOf(ss.get("id")));

            Map<String, Object> buyWanted=null;
            try {
                buyWanted=ps.checkAmountProduct(ckcParam);
            }catch(Exception e) {
                return "noneProduct";
            }
            if(buyWanted==null) {
                return "noneProduct";
            }

            int restAmt=Integer.parseInt(String.valueOf(buyWanted.get("amount")));

            if(restAmt<Integer.parseInt(String.valueOf(ss.get("amount")))) {
                ss.put("amount", String.valueOf(buyWanted.get("amount")));
                ss.put("flag", "true");
                errorFlag=true;
            }
        }

        if(errorFlag) {
            return cart;
        }

        user.put("userid", userid);

        int result = ms.insertOrderBook(user);
        if (result == 1) {
            for (LinkedHashMap<String, String> ss : cart) {
                ss.put("userid", userid);
                ss.put("pid", String.valueOf(ss.get("id")));
                ms.updateAmountProduct(ss);
                ss.put("id", String.valueOf(user.get("preid")));
                ms.insertOrderDetail(ss);
            }
            if (session.getAttribute("id") != null) {

                ms.deleteCartAll(userid);

            }

            return "success";
        }else {
            return "failed";
        }

    }

    @Login
    @GetMapping("/checkOrder")
    public Object checkOrder(String id, HttpSession session) {

        String userid = null;
        Map<String, String> accParam = new HashMap<>();
        userid=((Member) session.getAttribute("id")).getId();
        HashMap<String, String> account =new HashMap<>();
        account.put("userid", userid);
        account.put("id",  id);
        List<HashMap<String, Object>> cList = ms.selectOrderDetail(account);
        return cList;

    }



}

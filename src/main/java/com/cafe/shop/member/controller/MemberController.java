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
        String userid = null;
        int total = 0;
        int minTot = 0;
        List<Map<String, String>> putList = new ArrayList<Map<String, String>>();
        Map<String, String> accParam = new HashMap<>();

        userid=((Member) session.getAttribute("id")).getId();
        accParam.put("userid", ((Member) session.getAttribute("id")).getId());

        List<HashMap<String, Object>> cartSaved = ms.selectCart(userid);

        Map<String, List<LinkedHashMap<String, String>>> param = (Map<String, List<LinkedHashMap<String, String>>>) pocket;
        List<LinkedHashMap<String, String>> cartList = param.get("pocket");
        List<HashMap<String, Object>> idList=ps.checkAvailProductId();
        for (LinkedHashMap<String, String> one : cartList) {
            boolean avail=true;
            String flag = "false";
            int amount = 0;
            String cate = "";

            for (int i = 0; i < cartSaved.size(); i++) {
                if (String.valueOf(cartSaved.get(i).get("productid")).equals(String.valueOf(one.get("id")))) {
                    flag = "true";
                    amount = Integer.parseInt(String.valueOf(cartSaved.get(i).get("amount")));
                    cate = String.valueOf(cartSaved.get(i).get("catename"));
                }
                for( Map<String, Object> id: idList) {
                    String singleId=String.valueOf(id.get("id"));
                    if (String.valueOf(cartSaved.get(i).get("productid")).equals(singleId)) {
                        avail=false;
                    }
                }
                if(avail) {
                    return "avail";
                }
            }
            int added = Integer.parseInt(String.valueOf(one.get("amount")));

            if (cate.equals("쿠키"))
                minTot += added;

            Map<String, String> daoMap = new HashMap<>();
            daoMap.put("productid", String.valueOf(one.get("id")));
            daoMap.put("amount", added + "");
            daoMap.put("userid", userid);
            daoMap.put("flag", flag);

            if (flag.equals("false")) {
                cate = one.get("cate");
            }
            daoMap.put("cate", cate);

            if (cate.equals("쿠키")) {
                if (added > 5) {
                    return "amount";
                }
            }
            putList.add(daoMap);
        }

        if (minTot < 4&& minTot>0) {
            return "amount";
        }

        for (Map<String, String> cart : putList) {
            if (cart.get("cate").equals("쿠키"))
                total += Integer.parseInt(cart.get("amount"));
        }
        if (total > 14) {
            return "amount";
        }

        return "success";

    }

    @Login
    @PostMapping("/confirmRest")
    public Object confirmRest(@RequestBody(required = false) Object pocket, HttpSession session) {

        String userid = null;
        Member m=(Member) session.getAttribute("id");

        Map<String, String> accParam = new HashMap<>();
        userid=((Member) session.getAttribute("id")).getId();


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

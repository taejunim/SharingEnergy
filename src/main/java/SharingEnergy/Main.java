package SharingEnergy;


import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
import java.util.Map;

@Controller
public class Main {


    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private MainService mainService;

    @Autowired
    private SqlSession sqlSession ;

    @RequestMapping(value = "SharingEnergy/test.do")
    public String test() {

        System.out.println("adf");

        System.out.println("test");
        
        return "index";
    }



    //Summary Data Insert


    private Map<String,Object> makeResponseMap(String code, String result, String message) {

        Map<String,Object> resultMap = new HashMap<String, Object>();

        resultMap.put("code", code);
        resultMap.put("result", result);
        resultMap.put("message", message);

        return resultMap;

    }
}

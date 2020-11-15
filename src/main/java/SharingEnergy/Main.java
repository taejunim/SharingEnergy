package SharingEnergy;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

@Controller
public class Main {


    /** 로그인 화면 **/
    @RequestMapping(value = "test.do")
    public String Login(HttpServletRequest req) throws Exception {


        System.out.println("ignore test");

        return "index";
    }

}

package SharingEnergy;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

@Controller
public class Main {

    @RequestMapping(value = "postData.do")
    public String postData(HttpServletRequest req) throws Exception {


        System.out.println("ignore test");

        return "index";
    }

}

package org.example.server.controllers;



import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("")
public class HomeController {
    @RequestMapping(value = "/about",method = RequestMethod.GET)
    public String about(){
        return "about";
    }

    @RequestMapping(value = "/chef", method = RequestMethod.GET)
    public String chef(){return "chef";}

    @RequestMapping(value = "/menu", method = RequestMethod.GET)
    public String menu(){return "menu";}

    @RequestMapping(value = "/reservation", method = RequestMethod.GET)
    public String reservation(){return "reservation";}

    @RequestMapping(value = "/blog", method = RequestMethod.GET)
    public String blog(){return "blog";}

    @RequestMapping(value = "/contact", method = RequestMethod.GET)
    public String contact(){return "contact";}

    @RequestMapping(value = "/home", method = RequestMethod.GET)
    public String home(){
        return "test/home";
    }
}

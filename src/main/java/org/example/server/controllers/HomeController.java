package org.example.server.controllers;



import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("")
public class HomeController {

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String home(){
        return "/Web/index";
    }

    @RequestMapping(value = "/about",method = RequestMethod.GET)
    public String about(){
        return "/Web/about";
    }

    @RequestMapping(value = "/chef", method = RequestMethod.GET)
    public String chef(){return "/Web/chef";}

    @RequestMapping(value = "/menu", method = RequestMethod.GET)
    public String menu(){return "/Web/menu";}

    @RequestMapping(value = "/reservation", method = RequestMethod.GET)
    public String reservation(){return "/Web/reservation";}

    @RequestMapping(value = "/blog", method = RequestMethod.GET)
    public String blog(){return "/Web/blog";}

    @RequestMapping(value = "/contact", method = RequestMethod.GET)
    public String contact(){return "/Web/contact";}

    @RequestMapping(value = "/home", method = RequestMethod.GET)
    public String test(){
        return "test/home";
    }



}

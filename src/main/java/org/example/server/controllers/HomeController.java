package org.example.server.controllers;



import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("")
public class HomeController {

    @RequestMapping(value = "/en/", method = RequestMethod.GET)
    public String home(){
        return "/Web/index";
    }

    @RequestMapping(value = "/en/about",method = RequestMethod.GET)
    public String about(){
        return "/Web/about";
    }

    @RequestMapping(value = "/en/chef", method = RequestMethod.GET)
    public String chef(){return "/Web/chef";}

    @RequestMapping(value = "/en/menu", method = RequestMethod.GET)
    public String menu(){return "/Web/menu";}

    @RequestMapping(value = "/en/reservation", method = RequestMethod.GET)
    public String reservation(){return "/Web/reservation";}

    @RequestMapping(value = "/en/blog", method = RequestMethod.GET)
    public String blog(){return "/Web/blog";}

    @RequestMapping(value = "/en/contact", method = RequestMethod.GET)
    public String contact(){return "/Web/contact";}

    @GetMapping("/en/order")
    public String showOrder() {
        return "Web/order";
    }



}

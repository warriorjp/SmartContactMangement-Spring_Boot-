package com.smart.controller;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;


import com.smart.dao.UserRepository;
import com.smart.entity.User;
import com.smart.helper.Message;




@Controller
public class HomeController {
	
	@Autowired
	private BCryptPasswordEncoder PasswordEncoder;
	
	 @Autowired private UserRepository userRepository;
	 
	@GetMapping("/")
		public String home() {
		return "home";
	}

	
	@GetMapping("/about")
	public String about() {
		return "about";
	}
	
	
	@GetMapping("/signup")
	public String Signup(Model model) {
		model.addAttribute("title","register here");
		model.addAttribute("user",new User());
		return "signup";
		
	}
	
	@GetMapping("/sign")
	public String customeLogin(Model model) {
		model.addAttribute("title","Login Page");
		return "login";
	}
	
	@RequestMapping(value = "/do_register",method = RequestMethod.POST)
	public String registerUser(@Valid @ModelAttribute("user")User user,BindingResult result1,@RequestParam(value="agrement",defaultValue = "false") boolean agrement,Model model,
			HttpSession session) {
		

		
		try {
			if(!agrement) {
				System.out.println("You have not agreed the terms and conditions");
				throw new Exception("You have not agrred the terms and conditions");
			}
			
			if(result1.hasErrors()) {
				model.addAttribute("user",user);
				return "signup";
			}
			user.setRole("ROLE_USER");
			user.setEnabled(true);
			user.setImageUrl("default.png");
			user.setPassword(PasswordEncoder.encode(user.getPassword()));
			User result=this.userRepository.save(user);
			model.addAttribute("user",new User());
			session.setAttribute("message",new Message("Successfully Registered !!","alert-success"));
			return "signup";
			
			
		} catch (Exception e) {
			e.printStackTrace();
			model.addAttribute("user",user);
			session.setAttribute("message",new Message("something went wrong!!","alert-danger"));
			return "signup";
		}
		
	}
}

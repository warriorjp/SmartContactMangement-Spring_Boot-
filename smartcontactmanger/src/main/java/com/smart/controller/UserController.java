package com.smart.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;


import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.smart.dao.ContactRespository;
import com.smart.dao.UserRepository;
import com.smart.entity.Contact;
import com.smart.entity.User;
import com.smart.helper.Message;

import   org.springframework.data.domain.Page;




@Controller
@RequestMapping("/user")
public class UserController {

	@Autowired
	private UserRepository UserRepository;
	
	@Autowired
	private ContactRespository ContactRespository;
	
	@ModelAttribute
	public void addcommonData(Model model,Principal principal) {
		
		String userName=principal.getName();
		
		User user=this.UserRepository.getUserByUserName(userName);
			model.addAttribute(user);
	}
	
	//dashboard home
	@RequestMapping("/index")
	public String dashborad(Model model,Principal principal) {
	  System.out.println("hiii.."+principal.getName());
		return "normal/user_dashbord";
	}
	
	@GetMapping("/add-contact")
	public String openAddContactForm(Model model) {
		model.addAttribute("title","Add Contact");
		model.addAttribute("contact",new Contact());
		return "normal/add_contact_form";
	}
	
	
	// processing add contact data

	@PostMapping("/process-contact")
	public String processContact(@ModelAttribute Contact contact, @RequestParam("profileImage") MultipartFile file,
			Principal principal, HttpSession session) {

		try {
			// uploading file
			String name = principal.getName();
			User user = this.UserRepository.getUserByUserName(name);

			if (file.isEmpty()) {
				// if the file is empty try our message

				System.out.println("empty image .....");

			} else {
				contact.setImage(file.getOriginalFilename());
				File saveFile = new ClassPathResource("static/image").getFile();
				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + file.getOriginalFilename());
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				System.out.println("Image is uploaded");

			}

			contact.setUser(user);
			user.getContacts().add(contact);
			this.UserRepository.save(user);

			// message success......

			session.setAttribute("message", new Message("Your contact is added !!", "success"));

		} catch (Exception e) {
			System.out.println("ERROR" + e.getMessage());
			e.printStackTrace();

			// message error....
			session.setAttribute("message", new Message("Something went wrong !!", "danger"));
		}
		return "normal/add_contact_form";
	}
	
	/* show contact detail */
	@GetMapping("/show-contacts/{page}")
	public String showContacts(@PathVariable("page") Integer page,Model model, Principal principal) {
		model.addAttribute("title", "view contacts");
		
		  String username = principal.getName();
		  User user =
		  this.UserRepository.getUserByUserName(username);
		  PageRequest pageable= PageRequest.of(page,5);
		  Page<Contact> contacts =this.ContactRespository.findContactsByUser(user.getId(),pageable);
		  model.addAttribute("contacts", contacts);
		  model.addAttribute("currentPage",page);
		  model.addAttribute("totalPages",contacts.getTotalPages());
		 
		return "normal/show_contacts";
	}
	
}
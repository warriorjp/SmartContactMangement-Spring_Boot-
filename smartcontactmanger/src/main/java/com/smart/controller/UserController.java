package com.smart.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.Optional;

import javax.servlet.http.HttpSession;
import javax.websocket.Session;

import org.hibernate.sql.Delete;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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
	
	@Autowired
	private BCryptPasswordEncoder BCryptPasswordEncoder;
	
	
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
				contact.setImage("contact.png");

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
		System.out.println("hiii.....");
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
	
	/* showing particular contact details */
	@RequestMapping("/contact/{cId}")
	public String showContactDetail(@PathVariable("cId") Integer cId,Model model,Principal principal) {
		Optional<Contact> contactOptional=this.ContactRespository.findById(cId);
		Contact contact=contactOptional.get();
		String userName=principal.getName();
		User user=this.UserRepository.getUserByUserName(userName);
		if(user.getId()==contact.getUser().getId())
		{model.addAttribute(contact);}
		
		return "normal/contact";
		
	}
	
	
	/* Delete contact */
	
	@GetMapping("/delete/{cid}")
	public String deleteContact(@PathVariable("cid") Integer cid,Model model,HttpSession session) {
		Optional<Contact> conOptional=this.ContactRespository.findById(cid);
		Contact contact=conOptional.get();
		this.ContactRespository.delete(contact);
	    session.setAttribute("message",new Message("Contact deleted successfully....", "success"));
		return "redirect:/user/show-contacts/0";
	}
	
	
	/* update contact */
	@PostMapping("/update/{cid}")
	public String updateContact(@PathVariable("cid") Integer cid,Model model) {
	  Contact contact=this.ContactRespository.findById(cid).get();
	  model.addAttribute(contact);
		
		return "normal/update";
	}
	
	/* process-update-contact */
	@PostMapping("/process-update")
	public String updateProcess(@ModelAttribute Contact contact,@RequestParam("profileImage") MultipartFile file,
			Model model,HttpSession session,Principal principal) {
	try {
		Contact contact2=this.ContactRespository.findById(contact.getCid()).get();
		if(!file.isEmpty()) {
			
			/* delete old photo */
			System.out.println(contact2.getImage());
			
			/* update new photo */
			File saveFile = new ClassPathResource("static/image").getFile();
			Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + file.getOriginalFilename());
			Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
			contact.setImage(file.getOriginalFilename());
		}else {
			contact.setImage(contact2.getImage());
		}
		User user=this.UserRepository.getUserByUserName(principal.getName());
		contact.setUser(user);
		this.ContactRespository.save(contact);
		
		session.setAttribute("message",new Message("Your Contact Is Updated...","success"));
	} catch (Exception e) {
		e.printStackTrace();
	}	
	return "redirect:/user/contact/"+contact.getCid();	
	}
	
	@GetMapping("/profile")
	public String YourProfile() {
		
		return "normal/profile";
	}
	
	/* setting */
	
	@GetMapping("/settings")
	public String Setting() {
		return "normal/setting";
	}
	
	/* change password */
	
	@PostMapping("/changepassword")
	public String changePassword(@RequestParam("oldpassword") String oldpassword,@RequestParam("newpassword") String newpassword,Principal principal,HttpSession session) {
		
		String userName=principal.getName();
		User currentUser=this.UserRepository.getUserByUserName(userName);
		if(this.BCryptPasswordEncoder.matches(oldpassword,currentUser.getPassword())) {
			
			currentUser.setPassword(this.BCryptPasswordEncoder.encode(newpassword));
			this.UserRepository.save(currentUser);
			session.setAttribute("message", new Message("Your password is successfully change","success"));
			
		}else {
			session.setAttribute("message", new Message("Please enter correct old password !!","danger"));
		}
		return "redirect:/user/index";
	}
}
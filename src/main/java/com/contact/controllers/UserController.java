package com.contact.controllers;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.contact.dao.ContactRepo;
import com.contact.dao.UserRepo;
import com.contact.entities.Contact;
import com.contact.entities.User;
import com.contact.helper.Message;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
@RequestMapping(path = "/user")
public class UserController {

	@Autowired
	private UserRepo repo;

	@Autowired
	private ContactRepo conRepo;

	// Method to add common data to all the handlers
	@ModelAttribute
	public void addCommonData(Model model, Principal principal) {
		String userName = principal.getName();
		System.out.println("Username: " + userName);
		User userByUserName = this.repo.getUserByUserName(userName);
		System.out.println(userByUserName);
		model.addAttribute(userByUserName);
	}

	// Handler to show user dashboard
	@GetMapping(path = "/dashboard")
	public String dashboard(Model model, Principal principal) {
		model.addAttribute("title", "User dashboard");
		return "normal/user_dashboard";
	}

	// Handler to show the form to add contact
	@GetMapping(path = "/add-contact")
	public String addContactForm(Model model) {
		model.addAttribute("title", "Add Contact");
		model.addAttribute("contact", new Contact());
		return "normal/add_contact";
	}

	// Handler to process add contact form
	@PostMapping(path = "/process-contact")
	public String proccessContactForm(@Valid @ModelAttribute("contact") Contact contact,
			@RequestParam("profileImage") MultipartFile multipartFile, Model model, Principal principal,
			HttpSession httpSession) {

		try {
			String name = principal.getName();
			User user = this.repo.getUserByUserName(name);
			// processing and uploading file(profile pic)
			if (multipartFile.isEmpty()) {
				System.out.println("File is empty");
				contact.setImage("/ContactMate/src/main/resources/static/img/default_contact.png");
			} else {
				contact.setImage(multipartFile.getOriginalFilename());
				File file = new ClassPathResource("static/img").getFile();
				Path path = Paths.get(file.getAbsolutePath() + File.separator + multipartFile.getOriginalFilename());
				Files.copy(multipartFile.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				System.out.println("Image is uploaded");
			}
			contact.setUser(user);
			user.getContacts().add(contact);

			this.repo.save(user);

			System.out.println("Contact Data: " + contact);
			System.out.println("Contacts added to db");
			httpSession.setAttribute("message", new Message("Contact added successfully", "success"));
		} catch (Exception e) {
			System.out.println("Error: " + e.getMessage());
			e.printStackTrace();
			httpSession.setAttribute("message", new Message("Something went wrong, try again", "danger"));
		}
		return "normal/add_contact";
	}

	// Show contacts
	// per page 5 contacts
	@GetMapping(path = "/show-contacts/{page}")
	public String showContacts(@PathVariable("page") Integer page, Model model, Principal principal) {
		model.addAttribute("title", "Contacts");

//		String userName = principal.getName();
//		User userByUserName = this.repo.getUserByUserName(userName);
//		List<Contact> contacts = userByUserName.getContacts();
		String userName = principal.getName();
		User userByUserName = this.repo.getUserByUserName(userName);

		// Pagination properties
		Pageable pageable = PageRequest.of(page, 3);

		Page<Contact> contactsByUser = this.conRepo.findContactsByUser(userByUserName.getId(), pageable);
		model.addAttribute("contacts", contactsByUser);
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", contactsByUser.getTotalPages());
		return "normal/show_contacts";
	}

	// Showing Single contact details as per Contact ID

	@GetMapping(path = "/{c_id}/contact")
	public String showContactDetail(@PathVariable("c_id") Long c_id, Model model, Principal principal) {
		
		Optional<Contact> contactOptional = this.conRepo.findById(c_id);
		Contact contact = contactOptional.get();
		
		String userName = principal.getName();
		User userByUserName = this.repo.getUserByUserName(userName);
		
		if(userByUserName.getId()==contact.getUser().getId()) {
			model.addAttribute("contact", contact);
		}
		
		//model.addAttribute("contact", contact);
		
		model.addAttribute("title","Contact");
		System.out.println("Contact ID: "+c_id);
		return "normal/contact_detail";
	}
}

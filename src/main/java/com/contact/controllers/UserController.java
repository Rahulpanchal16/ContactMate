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
	private BCryptPasswordEncoder bCryptPasswordEncoder;

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

		if (userByUserName.getId() == contact.getUser().getId()) {
			model.addAttribute("contact", contact);
			model.addAttribute("title", contact.getName());
		}

		// model.addAttribute("contact", contact);

		model.addAttribute("title", "Contact");
		System.out.println("Contact ID: " + c_id);
		return "normal/contact_detail";
	}

	// Delete contact handler
	@GetMapping(path = "/delete/{c_id}")
	public String deleteContact(@PathVariable("c_id") Long id, Model model, Principal principal, HttpSession session) {
		Optional<Contact> contactById = this.conRepo.findById(id);
		Contact contact = contactById.get();

		String name = principal.getName();
		User userByUserName = this.repo.getUserByUserName(name);
		if (userByUserName.getId() == contact.getUser().getId()) {
//			contact.setUser(null);
//			this.conRepo.deleteById(id);
			User byUserName = this.repo.getUserByUserName(principal.getName());
			byUserName.getContacts().remove(contact);
			this.repo.save(byUserName);
			session.setAttribute("message", new Message("Contact deleted successfully", "success"));
		}

		return "redirect:/user/show-contacts/0";
	}

	// Editing the contact handler
	@PostMapping(path = "/edit-contact/{c_id}")
	public String editContact(@PathVariable("c_id") Long id, Model model) {
		model.addAttribute("title", "edit - ContactMate");
		Contact contact = this.conRepo.findById(id).get();
		model.addAttribute("contact", contact);
		return "normal/edit_contact";
	}

	// processing the new edited contact data

	@PostMapping(path = "/process-edit")
	public String updateProcess(@ModelAttribute Contact contact, @RequestParam("profileImage") MultipartFile file,
			Model model, HttpSession httpSetssion) {

//		try {
//			//check the image file
//			if(!file.isEmpty()) {
//				
//			}
//			
//			
//		} catch (Exception e) {
//			e.printStackTrace();
//		}

		System.out.println(contact.getName());
		long id = contact.getC_id();

		System.out.println("Contact Id: " + id);

		return "";
	}

	// Your profile section handler
	@GetMapping(path = "/profile")
	public String profile(Model model) {
		model.addAttribute("title", "profile - ContactMate");
		return "normal/profile";
	}

	// Open settings handler
	@GetMapping(path = "/settings")
	public String openSettings() {
		return "normal/settings";
	}

	// change password handler
	@PostMapping(path = "/change-password")
	public String changePassword(@RequestParam("oldpass") String oldpass, @RequestParam("newpass") String newpass,
			Principal principal,HttpSession session) {
		String user = principal.getName();
		User userByUserName = this.repo.getUserByUserName(user);
		String currentUserPassword = userByUserName.getPassword();
		if (this.bCryptPasswordEncoder.matches(oldpass, currentUserPassword)) {
			userByUserName.setPassword(this.bCryptPasswordEncoder.encode(newpass));
			this.repo.save(userByUserName);
			session.setAttribute("message", new Message("Password changed successfully", "success")); 
		} else {
			session.setAttribute("message", new Message("Incorrect password", "danger")); 

		}

		return "/normal/user_dashboard";
	}
}

package com.contact.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.contact.dao.UserRepo;
import com.contact.entities.User;
import com.contact.helper.Message;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
public class HomeController {

	@Autowired
	private PasswordEncoder passEncode;

	@Autowired
	private UserRepo userRepo;

	@GetMapping(path = "/")
	public String home(Model model) {
		model.addAttribute("title", "Home - ContactMate");
		return "home";
	}

	@GetMapping(path = "/about")
	public String about(Model model) {
		model.addAttribute("title", "about - ContactMate");
		return "about";
	}

	@GetMapping(path = "/signup")
	public String signUp(Model model) {
		model.addAttribute("title", "Signup - ContactMate");
		model.addAttribute("user", new User());
		return "signup";
	}

	@PostMapping(path = "/do_register")
	public String registerUser(@Valid @ModelAttribute("user") User user, BindingResult result, Model model,
			@RequestParam(value = "terms", defaultValue = "false") boolean terms, HttpSession session) {

		try {
			if (!terms) {
				System.out.println("Please check the terms and conditions");
				throw new Exception("Please check the terms and conditions");
			}
			if (result.hasErrors()) {
				System.out.println("Errors: " + result.toString());
				model.addAttribute("user", user);
				return "signup";
			}

			user.setRole("ROLE_USER");
			user.setEnabled(true);
			user.setImageUrl("default.png");
			user.setPassword(passEncode.encode(user.getPassword()));

			System.out.println("Terms" + terms);
			System.out.println("User: " + user);

			User resultUser = this.userRepo.save(user);

			model.addAttribute("user", new User());
			session.setAttribute("message", new Message("Registered Successfully", "alert-success"));
			return "signup";

		} catch (Exception e) {
			e.printStackTrace();
			model.addAttribute("user", user);
			session.setAttribute("message", new Message("Please check the terms and conditions", "alert-danger"));
			return "signup";
		}
		
	}
	
	@GetMapping(path = "/login")
	public String customLogin(Model model) {
		model.addAttribute("title","Login - ContactMate");
		return "login";
	}

}

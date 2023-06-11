package com.contact.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ForgotPasswordController {

	//Email id form handler
	@GetMapping(path = "/forgot")
	public String openEmailForm() {
		return "forgot_email";
	}
}

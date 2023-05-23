package com.contact.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.contact.dao.UserRepo;
import com.contact.entities.User;

public class UserDetailsServiceImpl implements UserDetailsService {

	@Autowired
	private UserRepo repo;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

		// fetching user from Database
		User userByUserName = this.repo.getUserByUserName(username);
		if (userByUserName == null) {
			throw new UsernameNotFoundException("Could not find the User");
		}

		CustomUserDetails customUserDetails = new CustomUserDetails(userByUserName);

		return customUserDetails;
	}

}

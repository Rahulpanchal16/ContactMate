package com.contact.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.contact.entities.Contact;

public interface ContactRepo extends JpaRepository<Contact, Long> {
	// pagination
	// Pageable will have two attributes ie. Current page & records (contacts in our case) per page
	@Query("from Contact as c where c.user.id= :userId")
	public Page<Contact> findContactsByUser(@Param("userId") long userId, Pageable pageable);

}

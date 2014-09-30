package com.ndi.app.model;

import com.fererlab.dto.Model;
import com.thoughtworks.xstream.annotations.XStreamAlias;

import javax.persistence.*;
import java.io.Serializable;


/**
 * The persistent class for the DEPARTMENT database table.
 * 
 */
@Entity
@XStreamAlias("department")
public class Department implements Serializable, Model {
	private static final Long serialVersionUID = 1L;
	private Long id;
	private String email;
	private String name;

	public Department() {
	}


	@Id
	@SequenceGenerator(name="DEPARTMENT_ID_GENERATOR", sequenceName="SEQ_DEPARTMENT")
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="DEPARTMENT_ID_GENERATOR")
	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}


	public String getEmail() {
		return this.email;
	}

	public void setEmail(String email) {
		this.email = email;
	}


	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}


}
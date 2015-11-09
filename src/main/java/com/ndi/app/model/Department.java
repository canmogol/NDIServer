package com.ndi.app.model;

import com.fererlab.dto.Model;

import javax.persistence.*;
import java.io.Serializable;


/**
 * The persistent class for the DEPARTMENT database table.
 */
@Entity
@NamedQueries({
        @NamedQuery(name = "Department.findAll", query = "SELECT d FROM Department d"),
        @NamedQuery(name = "Department.findName", query = "SELECT d FROM Department d where d.name=:name")
})
public class Department implements Serializable, Model {

    public final static String FIND_ALL = "Department.findAll";
    public final static String FIND_NAME = "Department.findName";

    private static final Long serialVersionUID = 1L;
    private Long id;
    private String email;
    private String name;

    public Department() {
    }


    @Id
    @SequenceGenerator(name = "DEPARTMENT_ID_GENERATOR", sequenceName = "SEQ_DEPARTMENT", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "DEPARTMENT_ID_GENERATOR")
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
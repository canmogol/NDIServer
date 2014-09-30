package com.fererlab.dto;

import javax.persistence.*;
import java.io.Serializable;

/**
 * acm
 */
@Entity
@Cacheable(false)
@Table(name = "G_ACTION_MODEL")
public class GActionModel implements Serializable, Model {
    private static final Long serialVersionUID = 1L;
    private Long id;
    private String name;
    private String content;

    @Id
    @SequenceGenerator(name="GACTION_ID_GENERATOR", sequenceName="SEQ_GACTIONMODEL")
    @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="GACTION_ID_GENERATOR")
    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Column(length = 4000)
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

}

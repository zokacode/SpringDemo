package com.example.model;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.stereotype.Component;

@Data
@Entity
@Table(name = "tb_person")
@EntityListeners(AuditingEntityListener.class)
public class PersonModel {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "name",
			columnDefinition = "名字")
	private String name;

	@Column(name = "nickname",
			columnDefinition = "暱稱")
	private String nickname;

	@Column(name = "sex",
			columnDefinition = "性別")
	private String sex;

	@Column(name = "birthday",
			columnDefinition = "生日")
	private String birthday;

	@Column(name = "description",
			columnDefinition = "備註")
	private String description;

	@Column(name = "create_time",
			columnDefinition = "建立時間")
	@CreatedDate
	private String create_time;

	public Long getId() {
		return id;
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
	public String getNickname() {
		return nickname;
	}
	public void setNickname(String nickname) {
		this.nickname = nickname;
	}
	public String getSex() {
		return sex;
	}
	public void setSex(String sex) {
		this.sex = sex;
	}
	public String getBirthday() {
		return birthday;
	}
	public void setBirthday(String birthday) {
		this.birthday = birthday;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getCreate_time() {
		return create_time;
	}
	public void setCreate_time(String create_time) {
		this.create_time = create_time;
	}

}

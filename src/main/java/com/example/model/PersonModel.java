package com.example.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Entity
@Table(name = "tb_person")
@Data
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PersonModel {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "name",
			columnDefinition = "名字",
			nullable = false)
	private String name;

	@Column(name = "nickname",
			columnDefinition = "暱稱")
	private String nickname;

	@Column(name = "sex",
			columnDefinition = "性別 COMMENT '1:男, 2:女'",
			nullable = false)
	private Integer sex;

	@Column(name = "birthday",
			columnDefinition = "生日")
	private String birthday;

	@Column(name = "description",
			columnDefinition = "備註")
	private String description;

	@CreatedDate
	@Column(name = "created_at",
			columnDefinition = "建立時間", updatable = false)
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Instant created_at;

	@LastModifiedDate
	@Column(name = "updated_at",
			columnDefinition = "更新時間")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Instant updated_at;
}

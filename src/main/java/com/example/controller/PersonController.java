package com.example.controller;

import java.util.List;

import com.example.request.PersonCreateRequest;
import com.example.request.PersonUpdateRequest;
import com.example.response.BaseResp;
import com.example.response.PersonResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.example.model.PersonModel;
import com.example.service.PersonService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/person")
@Validated
@RequiredArgsConstructor
public class PersonController {

	private final PersonService personService;

	@GetMapping("/all")
	public BaseResp<List<PersonResponse>> getPerson() {
		// 取得ALL資料
		return personService.getAllPerson();
	}

	@GetMapping("/{id}")
	public BaseResp<PersonResponse> getOnePerson(@PathVariable Long id) {
		return personService.getOnePerson(id);
	}

	@PostMapping("/create-person")
	public BaseResp<Void> createPerson(@Valid @RequestBody PersonCreateRequest req) {
		return personService.createPerson(req);
	}

	@PutMapping("/edit-person/{id}")
	/* 修改資料儲存Api */
	public BaseResp<Void> editPerson(@PathVariable Long id, @Valid @RequestBody PersonUpdateRequest req) {
		req.setId(id);
		return personService.editPerson(req);
	}

	@DeleteMapping("/delete-person/{id}")
	/* 刪除資料Api */
	public BaseResp<Void> delPerson(@PathVariable Long id) {
		return personService.deletePerson(id);
	}

}

package com.example.service;

import java.util.*;

import com.example.enums.SystemCode;
import com.example.request.PersonCreateRequest;
import com.example.request.PersonUpdateRequest;
import com.example.response.BaseResp;
import com.example.response.PersonResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.example.model.PersonModel;
import com.example.repository.PersonRepository;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PersonService {

	private final JdbcTemplate jdbcTemplate;
	private final PersonRepository personRepository;

	/* 取得Person全部資料 */
	public BaseResp<List<PersonResponse>> getAllPerson() {
		log.info("getAllPerson 開始執行");
		try {
			List<PersonModel> personList = personRepository.findAll();
			List<PersonResponse> responseList = personList.stream()
					.map(person -> PersonResponse.builder()
						.id(person.getId())
						.name(person.getName())
						.nickname(person.getNickname())
						.sex(person.getSex())
						.birthday(person.getBirthday())
						.description(person.getDescription())
						.createTime(person.getCreated_at())
						.build())
					.toList();
			return BaseResp.success(responseList);
		} catch (Exception e) {
			log.error("getAllPerson 執行失敗", e);
			return BaseResp.error(SystemCode.SYSTEM_ERROR);
		} finally {
			log.info("getAllPerson 執行結束");
		}
	}

	/* 取得單獨資料 */
	public BaseResp<PersonResponse> getOnePerson(Long id) {
		log.info("getOnePerson 開始執行, id: {}", id);
		try {
			return personRepository.findById(id)
				.map(person -> {
					PersonResponse resp = PersonResponse.builder()
						.id(person.getId())
						.name(person.getName())
						.nickname(person.getNickname())
						.sex(person.getSex())
						.birthday(person.getBirthday())
						.description(person.getDescription())
						.createTime(person.getCreated_at())
						.build();
					return BaseResp.success(resp);
				})
				.orElseThrow(() -> new RuntimeException("查無此人員資料"));
		} catch (Exception e) {
			log.error("getOnePerson 執行失敗, id: {}", id, e);
			return BaseResp.error(SystemCode.SYSTEM_ERROR);
		} finally {
			log.info("getOnePerson 執行結束, id: {}", id);
		}
	}

	/* 新增資料 */
	@Transactional
	public BaseResp<Void> createPerson(PersonCreateRequest req) {
		log.info("createPerson 開始執行");
		try {
			// 驗證
			if (req.getName() == null || req.getName().trim().isEmpty()) {
				return BaseResp.error(SystemCode.SYSTEM_ERROR, "姓名不能為空");
			}
			PersonModel person = PersonModel.builder()
					.name(req.getName())
					.nickname(req.getNickname())
					.sex(req.getSex())
					.birthday(req.getBirthday())
					.description(req.getDescription())
					.build();
			personRepository.save(person);

			return BaseResp.success();
		} catch (Exception e) {
			log.error("createPerson 執行錯誤", e);
			return BaseResp.error(SystemCode.SYSTEM_ERROR);
		} finally {
			log.info("createPerson 執行結束");
		}
	}

	/* 修改資料 */
	@Transactional
	public BaseResp<Void> editPerson(PersonUpdateRequest req) {
		try {
			log.info("editPerson 開始執行, id: {}", req.getId());

			// 寫法一
			// 檢查是否存在
			// PersonModel existingPerson = personRepository.findById(req.getId())
			// 		.orElseThrow(() -> new RuntimeException("查無此資料"));
			// PersonModel updatedPerson = PersonModel.builder()
			// 		.id(req.getId())
			// 		.name(req.getName())
			// 		.nickname(req.getNickname())
			// 		.sex(req.getSex())
			// 		.birthday(req.getBirthday())
			// 		.description(req.getDescription())
			// 		.build();

		    // personRepository.save(updatedPerson);
			// return BaseResp.success();

			// 寫法二
			return personRepository.findById(req.getId())
				.map(existingPerson -> {
					// 只更新非空欄位
					Optional.ofNullable(req.getName()).ifPresent(existingPerson::setName);
					Optional.ofNullable(req.getNickname()).ifPresent(existingPerson::setNickname);
					Optional.ofNullable(req.getSex()).ifPresent(existingPerson::setSex);
					Optional.ofNullable(req.getBirthday()).ifPresent(existingPerson::setBirthday);
					Optional.ofNullable(req.getDescription()).ifPresent(existingPerson::setDescription);

					personRepository.save(existingPerson);
					return BaseResp.success();
				})
				.orElseThrow(() -> new RuntimeException("查無此資料"));

		} catch (Exception e) {
			log.error("修改人員資料失敗, id: {}", req.getId(), e);
			return BaseResp.error(SystemCode.SYSTEM_ERROR);
		} finally {
			log.info("editPerson 執行結束");
		}
	}

	/* 刪除資料 */
	@Transactional
	public BaseResp<Void> deletePerson(Long id) {
		log.info("deletePerson 開始執行, id: {}", id);
		try {
			PersonModel person = personRepository.findById(id)
					.orElseThrow(() -> new RuntimeException("查無此資料"));

			personRepository.delete(person);

			return BaseResp.success();

			// 軟刪除寫法
			// return personRepository.findById(id)
			// 		.map(person -> {
			// 			person.setDeleted(true);
			// 			person.setDeleteTime(Instant.now());
			// 			personRepository.save(person);
			// 			return BaseResp.success();
			// 		})
			// 		.orElseThrow(() -> new RuntimeException("查無此人員資料"));

		} catch (Exception e) {
			log.error("刪除人員資料失敗, id: {}", id, e);
			return BaseResp.error(SystemCode.SYSTEM_ERROR);
		} finally {
			log.info("deletePerson 執行結束, id: {}", id);
		}
	}

}
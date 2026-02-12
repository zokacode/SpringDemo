package com.example.service;

import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.repository.PersonRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.example.model.PersonModel;

@Service
@RequiredArgsConstructor
@Slf4j
public class PersonService {

	private PersonModel personModel;
	private final JdbcTemplate jdbcTemplate;
	private final PersonRepository personRepository;

	private PrintStream ps = new PrintStream(System.out);

	//時間轉換用
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	/* 取得Person全部資料 */
	public Map<String, Object> getAllPerson() {
		// TODO Auto-generated method stub
		System.out.println("getAllPerson開始 " + sdf.format(new Date()));
		System.out.println("取得Person全部會員資料..");
		Map<String, Object> rs=new HashMap<String, Object>();
		StringBuilder sql = new StringBuilder("select id, name, nickname, sex,"
											+ " birthday, description,"
											+ " date_format(create_time, '%Y-%m-%d %H:%m:%s') as create_time"
											+ " from tb_person");
		rs.put("data",jdbcTemplate.queryForList(sql.toString()));

		List<Map<String, Object>> ms;
		ms = jdbcTemplate.queryForList(sql.toString());
		
		System.out.println(rs.get("data"));
		System.out.println("getAllPerson結束 " + sdf.format(new Date()));

		return rs;
	}

	/* 取得單獨資料 */
	public Map<String, Object> getOnePerson(String id) {
		// TODO Auto-generated method stub
		System.out.println("getPerson開始 " + sdf.format(new Date()));
		System.out.println("取得Person : "+id+" 會員資料..");
		Map<String, Object> rs=new HashMap<String, Object>();
		StringBuilder sql = new StringBuilder("SELECT id, name, nickname, sex,"
											+ " birthday, description,"
											+ " date_format(create_time, '%Y-%M-%d %H:%m:%s') as create_time"
											+ " FROM tb_person"
											+ " WHERE id = "+id);
		rs.put("data",jdbcTemplate.queryForList(sql.toString()));
		
		List<Map<String, Object>> ms;
		ms = jdbcTemplate.queryForList(sql.toString());
		
		System.out.println(rs);
		System.out.println("getPerson結束 " + sdf.format(new Date()));
		
		return rs;
	}

	/* 新增資料 */
	public void addPerson(String name, String nickname, String sex, String birthday, String description) throws Exception{

	    String date = sdf.format(new Date());
		ps.println("addPerson開始 "+ date);

		String sql = "INSERT INTO tb_person ("
					+ "name, nickname, sex, birthday, description, create_time)"
					+ "values(?, ?, ?, ?, ?, ?)";
		Object[] params = new Object[] {name, nickname, sex, birthday, description, date};

		try {
			ps.println(sql);
			for (int i = 0; i < params.length; i++) {
				ps.print(params[i]+", ");
			}
			jdbcTemplate.update(sql, params);

			personModel.setName(name);
			personModel.setNickname(nickname);
			personModel.setSex(sex);
			personModel.setBirthday(birthday);
			personModel.setDescription(description);
			personModel.setCreate_time(date);

			System.out.println("\n"+name+":"+nickname+":"+sex+":"+birthday+":"+description+":"+date);
			System.out.println("addPerson結束 "+ date);
		} catch (Exception e) {
			e.printStackTrace();
			ps.println(e.getMessage());
		}
	}

	/* 取得資料 並將資料新增至PersonModel */
	public List<Map<String, Object>> selPerson() {
		System.out.println("selPerson開始 " + sdf.format(new Date()));
		System.out.println("取得ID "+personModel.getId()+" Person會員資料..");
		Object[] params = new Object[]{personModel.getId()};

		List<Map<String, Object>> rs=new ArrayList<Map<String, Object>>();
		StringBuilder sql = new StringBuilder("SELECT id, name, nickname, sex, "
											+ "birthday, description, date_format(create_time, '%Y-%m-%d %H:%m:%s') as create_time "
											+ "FROM tb_person WHERE ID = ?");
		if (params != null) {
			ps.println(sql+":"+params[0].toString());
		} else {
			ps.println(sql+":"+params.toString());
		}
		
		rs = jdbcTemplate.queryForList(sql.toString(), params);
		for (Map<String, Object> i : rs) {
			for (Map.Entry<String, Object> j : i.entrySet()) {
				System.out.printf("rs Key->%s, Value->%S ", j.getKey(), j.getValue());

				switch (j.getKey().toString()) {
				case "id":
					ps.println("1");
					if (j.getValue()==null) {
						break;
					}
					personModel.setId(j.getValue().toString());
					break;
				case "name":
					ps.println("2");
					if (j.getValue()==null) {
						break;
					}
					personModel.setName(j.getValue().toString());
					break;
				case "nickname":
					ps.println("3");
					if (j.getValue()==null) {
						break;
					}
					personModel.setNickname(j.getValue().toString());
					break;
				case "sex":
					ps.println("4");
					if (j.getValue()==null) {
						break;
					}
					personModel.setSex(j.getValue().toString());
					break;
				case "birthday":
					ps.println("5");
					if (j.getValue()==null) {
						break;
					}
					personModel.setBirthday(j.getValue().toString());
					break;
				case "description":
					ps.println("6");
					if (j.getValue()==null) {
						break;
					}
					personModel.setDescription(j.getValue().toString());
					break;
				case "create_time":
					ps.println("7");
					if (j.getValue()==null) {
						break;
					}
					personModel.setCreate_time(j.getValue().toString());
					break;
				default:
					break;
				}
			}
		}
		System.out.println();
		System.out.println(rs);
		System.out.println("selPerson結束 " + sdf.format(new Date()));

		return rs;
	}

	/* 修改資料 */
	public void editPerson(String id, String name, String nickname, String sex, String birthday, String description) throws Exception{

	    String date = sdf.format(new Date());
		ps.println("editPerson開始 " + date);

		String sql = "UPDATE tb_person SET "
				+ "name = ?, nickname = ?, sex = ?, birthday = ?, description = ?, create_time = ? "
				+ "WHERE id = " + id;
		Object[] params = new Object[] {name, nickname, sex, birthday, description, date};
		
		try {
			jdbcTemplate.update(sql, params);
			ps.println(sql+params);
			selPerson();
			ps.println("editPerson結束 " + date);
		} catch (Exception e) {
			// TODO: handle exception
			ps.println(e.getMessage());
			e.printStackTrace();
		}
	}

	/* 刪除資料 */
	public void delPerson(String id) throws Exception {
		try {
			log.info("delPerson start: {}", sdf.format(new Date()));
			Long modelId = Long.parseLong(id);
			personRepository.deleteById(modelId);
		} catch (Exception e) {
			log.error("刪除人員資料失敗, id: {}", id, e);
			throw e;
		} finally {
			log.info("delPerson end: {}", sdf.format(new Date()));
		}
	}

}

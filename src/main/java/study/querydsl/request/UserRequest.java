package study.querydsl.request;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserRequest {
	private String name;
	private int age;

	public UserRequest(String name, int age) {
		this.name = name;
		this.age = age;
	}
}

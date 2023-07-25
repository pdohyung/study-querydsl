package study.querydsl.request;

import com.querydsl.core.annotations.QueryProjection;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MemberRequest {

	private String username;
	private int age;

	@QueryProjection
	public MemberRequest(String username, int age) {
		this.username = username;
		this.age = age;
	}
}

package study.querydsl.entity;

import static org.junit.jupiter.api.Assertions.*;
import static study.querydsl.entity.QMember.*;

import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

import com.querydsl.core.QueryResults;
import com.querydsl.jpa.impl.JPAQueryFactory;

import jakarta.persistence.EntityManager;

@SpringBootTest
@Transactional
class MemberTest {

	@Autowired
	EntityManager em;

	JPAQueryFactory queryFactory;

	@BeforeEach
	public void before(){
		queryFactory = new JPAQueryFactory(em);
		Team teamA = new Team("teamA");
		Team teamB = new Team("teamB");

		em.persist(teamA);
		em.persist(teamB);

		Member member1 = new Member("member1", 10, teamA);
		Member member2 = new Member("member2", 20, teamA);

		Member member3 = new Member("member3", 30, teamB);
		Member member4 = new Member("member4", 40, teamB);

		em.persist(member1);
		em.persist(member2);
		em.persist(member3);
		em.persist(member4);

	}

	@Test
	public void startQuerydsl1(){

		Member findMember = queryFactory
			.select(member)
			.from(member)
			.where(member.username.eq("member1"))
			.fetchOne();

		assertEquals(findMember.getUsername(), "member1");
	}

	@Test
	public void search(){
		Member findMember = queryFactory
			.selectFrom(member)
			.where(member.username.eq("member1")
				.and(member.age.eq(10)))
			.fetchOne();

		assertEquals(findMember.getUsername(), "member1");
	}

	@Test
	public void searchAndParam(){
		Member findMember = queryFactory
			.selectFrom(member)
			.where(member.username.eq("member1"),
					member.age.eq(10))
			.fetchOne();

		assertEquals(findMember.getUsername(), "member1");
	}

	@Test
	public void getResult(){
		// List<Member> fetch = queryFactory
		// 	.selectFrom(member)
		// 	.fetch();
		//
		// Member member1 = queryFactory
		// 	.selectFrom(member)
		// 	.fetchOne();
		//
		// Member member2 = queryFactory
		// 	.selectFrom(member)
		// 	.fetchFirst();

		// QueryResults<Member> fetchResults = queryFactory
		// 	.selectFrom(member)
		// 	.fetchResults();
		//
		// long total = fetchResults.getTotal();
		// List<Member> results = fetchResults.getResults();

		long count = queryFactory
			.selectFrom(member)
			.fetchCount();

	}

	@Test
	 public void sort(){
		em.persist(new Member(null, 100));
		em.persist(new Member("member5", 100));
		em.persist(new Member("member6", 100));

		List<Member> fetch = queryFactory.selectFrom(member)
			.where(member.age.eq(100))
			.orderBy(member.age.desc(), member.username.asc().nullsLast())
			.fetch();

		assertEquals(fetch.get(0).getUsername(), "member5");
		assertEquals(fetch.get(1).getUsername(), "member6");
		assertEquals(fetch.get(2).getUsername(), null);
	}

}
package study.querydsl.entity;

import static org.junit.jupiter.api.Assertions.*;
import static study.querydsl.entity.QMember.*;
import static study.querydsl.entity.QTeam.*;

import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;

import jakarta.persistence.EntityManager;

@SpringBootTest
@Transactional
class MemberTest {

	@Autowired
	EntityManager em;

	JPAQueryFactory queryFactory;

	@BeforeEach
	public void before() {
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
	public void startQuerydsl1() {

		Member findMember = queryFactory
			.select(member)
			.from(member)
			.where(member.username.eq("member1"))
			.fetchOne();

		assertEquals(findMember.getUsername(), "member1");
	}

	@Test
	public void search() {
		Member findMember = queryFactory
			.selectFrom(member)
			.where(member.username.eq("member1")
				.and(member.age.eq(10)))
			.fetchOne();

		assertEquals(findMember.getUsername(), "member1");
	}

	@Test
	public void searchAndParam() {
		Member findMember = queryFactory
			.selectFrom(member)
			.where(member.username.eq("member1"),
				member.age.eq(10))
			.fetchOne();

		assertEquals(findMember.getUsername(), "member1");
	}

	@Test
	public void getResult() {
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
	public void sort() {
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

	@Test
	public void paging1() {
		List<Member> result = queryFactory
			.selectFrom(member)
			.orderBy(member.username.desc())
			.offset(1)
			.limit(2)
			.fetch();

		assertEquals(result.size(), 2);
		assertEquals(result.get(0).getUsername(), "member3");
		assertEquals(result.get(1).getUsername(), "member2");
	}

	@Test
	public void paging2() {
		QueryResults<Member> queryResults = queryFactory
			.selectFrom(member)
			.orderBy(member.username.desc())
			.offset(1)
			.limit(2)
			.fetchResults();

		assertEquals(queryResults.getTotal(), 4);
		assertEquals(queryResults.getLimit(), 2);
		assertEquals(queryResults.getOffset(), 1);
		assertEquals(queryResults.getResults().size(), 2);
	}

	@Test
	public void aggregation(){
		List<Tuple> result = queryFactory.select(
				member.count(),
				member.age.sum(),
				member.age.avg(),
				member.age.max(),
				member.age.min()
			).from(member)
			.fetch();

		Tuple tuple = result.get(0);
		assertEquals(tuple.get(member.count()),  4);
		assertEquals(tuple.get(member.age.sum()), 100);
		assertEquals(tuple.get(member.age.avg()), 25);
		assertEquals(tuple.get(member.age.max()), 40);
		assertEquals(tuple.get(member.age.min()), 10);
	}

	@Test
	public void group(){
		List<Tuple> result = queryFactory.select(team.name, member.age.avg())
			.from(member)
			.join(member.team, team)
			.groupBy(team.name)
			.fetch();

		Tuple teamA = result.get(0);
		Tuple teamB = result.get(1);

		assertEquals(teamA.get(team.name), "teamA");
		assertEquals(teamA.get(member.age.avg()), 15);

		assertEquals(teamB.get(team.name), "teamB");
		assertEquals(teamB.get(member.age.avg()), 35);
	}
}
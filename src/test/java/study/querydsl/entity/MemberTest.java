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
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceUnit;

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
	public void aggregation() {
		List<Tuple> result = queryFactory.select(
				member.count(),
				member.age.sum(),
				member.age.avg(),
				member.age.max(),
				member.age.min()
			).from(member)
			.fetch();

		Tuple tuple = result.get(0);
		assertEquals(tuple.get(member.count()), 4);
		assertEquals(tuple.get(member.age.sum()), 100);
		assertEquals(tuple.get(member.age.avg()), 25);
		assertEquals(tuple.get(member.age.max()), 40);
		assertEquals(tuple.get(member.age.min()), 10);
	}

	@Test
	public void group() {
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

	@Test
	public void join() {
		List<String> membersTeamA = queryFactory.select(
				member.username)
			.from(member)
			.join(member.team, team)
			.where(team.name.eq("teamA"))
			.fetch();

		assertEquals(membersTeamA.size(), 2);
		assertEquals(membersTeamA.get(0), "member1");
		assertEquals(membersTeamA.get(1), "member2");
	}

	@Test
	public void theta_join() {
		em.persist(new Member("teamA"));
		em.persist(new Member("teamB"));

		List<Member> result = queryFactory
			.select(member)
			.from(member, team)
			.where(member.username.eq(team.name))
			.fetch();

		assertEquals(result.get(0).getUsername(), "teamA");
		assertEquals(result.get(1).getUsername(), "teamB");
	}

	@Test
	public void left_join_on() {
		List<Tuple> result = queryFactory.select(member, team)
			.from(member)
			.leftJoin(member.team, team).on(team.name.eq("teamA"))
			.fetch();
		for (Tuple tuple : result) {
			System.out.println("tuple = " + tuple);
		}
	}

	@Test
	public void left_join_on_no_relation() {
		em.persist(new Member("teamA"));
		em.persist(new Member("teamB"));

		List<Tuple> result = queryFactory
			.select(member, team)
			.from(member)
			.leftJoin(team).on(member.username.eq(team.name))
			.fetch();

		for (Tuple tuple : result) {
			System.out.println("tuple = " + tuple);
		}
	}

	@PersistenceUnit
	EntityManagerFactory emf;

	@Test
	public void fetch_join_no() {
		em.flush();
		em.clear();

		Member findMember = queryFactory
			.selectFrom(member)
			.where(member.username.eq("member1"))
			.fetchOne();

		boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
		Assertions.assertThat(loaded).as("패치 조인 미적용").isFalse();
	}

	@Test
	public void fetch_join_use() {
		em.flush();
		em.clear();

		Member findMember = queryFactory
			.selectFrom(member)
			.join(member.team, team).fetchJoin()
			.where(member.username.eq("member1"))
			.fetchOne();

		boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
		Assertions.assertThat(loaded).as("패치 조인 미적용").isTrue();
	}

	@Test
	public void subQuery() {
		QMember memberSub = new QMember("memberSub");

		List<Member> result = queryFactory.selectFrom(member)
			.where(member.age.eq(
				JPAExpressions.select(memberSub.age.max())
					.from(memberSub)
			)).fetch();

		assertEquals(result.get(0).getAge(), 40);
	}

	@Test
	public void subQueryGoe() {
		QMember memberSub = new QMember("memberSub");

		List<Member> result = queryFactory.selectFrom(member)
			.where(member.age.goe(
				JPAExpressions
					.select(memberSub.age.avg())
					.from(memberSub)
			)).fetch();

		assertEquals(result.get(0).getAge(), 30);
		assertEquals(result.get(1).getAge(), 40);
	}

	@Test
	public void subQueryIn() {
		QMember memberSub = new QMember("memberSub");

		List<Member> result = queryFactory.selectFrom(member)
			.where(member.age.in(
				JPAExpressions.select(memberSub.age)
					.from(memberSub)
					.where(memberSub.age.gt(10))
			)).fetch();

		Assertions.assertThat(result).extracting("age")
			.containsExactly(20, 30, 40);
	}

	@Test
	public void selectSubQuery() {
		QMember memberSub = new QMember("memberSub");

		List<Tuple> result = queryFactory
			.select(member.username,
				JPAExpressions.select(memberSub.age.avg())
					.from(memberSub)
			).from(member)
			.fetch();

		//System.out.println(result.get(0).get(member.username));
		System.out.println("result = " + result);
	}

	@Test
	public void basicCase() {

		List<String> result = queryFactory.select(member.age
				.when(10).then("열살")
				.when(20).then("스무살")
				.otherwise("기타"))
			.from(member)
			.fetch();

		System.out.println(result);
	}

	@Test
	public void complexCase() {
		List<String> result = queryFactory.select(new CaseBuilder()
				.when(member.age.between(0, 20)).then("0~20살")
				.when(member.age.between(21, 30)).then("21~30살")
				.otherwise("기타"))
			.from(member)
			.fetch();

		System.out.println(result);
	}

	@Test
	public void orderByCase() {

		NumberExpression<Integer> rankPath = new CaseBuilder()
			.when(member.age.between(0, 20)).then(2)
			.when(member.age.between(21, 30)).then(1)
			.otherwise(3);

		List<Tuple> result = queryFactory.select(member.username, member.age, rankPath)
			.from(member)
			.orderBy(rankPath.desc())
			.fetch();

		System.out.println("result = " + result);
	}

	@Test
	public void constant() {
		List<Tuple> result = queryFactory
			.select(member.username, Expressions.constant("A"))
			.from(member)
			.fetch();

		System.out.println("result = " + result);
	}

	@Test
	public void concat() {
		List<String> result = queryFactory
			.select(member.username.concat("_").concat(member.age.stringValue()))
			.from(member)
			.where(member.username.eq("member1"))
			.fetch();
		System.out.println("result = " + result);
	}
}
package study.querydsl;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

import com.querydsl.jpa.impl.JPAQueryFactory;

import jakarta.persistence.EntityManager;
import study.querydsl.entity.HelloEntity;
import study.querydsl.entity.QHelloEntity;

@SpringBootTest
@Transactional
@Commit
class QuerydslApplicationTests {

	@Autowired
	EntityManager em;

	@Test
	void contextLoads() {
		HelloEntity helloEntity = new HelloEntity();
		em.persist(helloEntity);

		JPAQueryFactory query = new JPAQueryFactory(em);
		QHelloEntity qHelloEntity = QHelloEntity.helloEntity;

		HelloEntity result = query
			.selectFrom(qHelloEntity)
			.fetchOne();

		Assertions.assertThat(result).isEqualTo(helloEntity);
		Assertions.assertThat(result.getId()).isEqualTo(helloEntity.getId());
	}

}

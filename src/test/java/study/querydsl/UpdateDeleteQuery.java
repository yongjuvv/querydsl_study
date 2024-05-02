package study.querydsl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.Team;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static study.querydsl.entity.QMember.*;

@SpringBootTest
@Transactional
public class UpdateDeleteQuery {

    @PersistenceContext
    EntityManager em;
    JPAQueryFactory query;

    @BeforeEach
    void testEntity() {
        query = new JPAQueryFactory(em);
        Team teamA = new Team("TeamA");
        Team teamB = new Team("TeamB");
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

        em.flush();
        em.clear();
    }

    @Test
    void bulkUpdate() {
        long count = query
                .update(member)
                .set(member.username, "비회원")
                .where(member.age.eq(10))
                .execute();

        assertThat(count).isEqualTo(1);
    }

    @Test
    void bulkAdd() {
        query
                .update(member)
                .set(member.age, member.age.add(1))
                .execute();

        List<Member> result = query.selectFrom(member).fetch();

        for (Member member1 : result) {
            System.out.println(member1.toString());
        }
    }

    @Test
    void bulkMultiply() {
        query
                .update(member)
                .set(member.age, member.age.multiply(2))
                .execute();

        List<Member> result = query.selectFrom(member).fetch();

        for (Member member1 : result) {
            System.out.println(member1.toString());
        }
    }

    @Test
    void bulkDelete() {
        query
                .delete(member)
                .where(member.age.loe(19))
                .execute();

        List<Member> result = query.selectFrom(member).fetch();

        for (Member member1 : result) {
            System.out.println(member1.toString());
        }
    }

//    @Test
//    @DisplayName("벌크 수정 연산 후 데이터 가져오기 - 영속성 컨텍스트에서 가져오기에 반영이 안됨")
//    void bulkUpdateAndFetch() {
//        query.update(member)
//                .set(member.username, "비회원")
//                .where(member.age.eq(10))
//                .execute();
//
//        List<Member> result = query
//                .selectFrom(member)
//                .fetch();
//
//        for (Member member1 : result) {
//            System.out.println(member1.toString());
//        }
//    }
}

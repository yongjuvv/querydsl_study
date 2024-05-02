package study.querydsl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.Team;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static study.querydsl.entity.QMember.*;

@Transactional
@SpringBootTest
public class DynamicQueryTest {
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
    void dynamicQuery_BooleanBuilder1() {
        String usernameParam = "member1";
        Integer ageParam = 10;

        List<Member> members = searchMember1(usernameParam, ageParam);

        assertThat(members.size()).isEqualTo(1);
        assertThat(members.get(0).getUsername()).isEqualTo(usernameParam);
        assertThat(members.get(0).getAge()).isEqualTo(ageParam);
    }
    @Test
    void dynamicQuery_BooleanBuilder2() {
        String usernameParam = "member1";

        List<Member> members = searchMember1(usernameParam, null);

        assertThat(members.size()).isEqualTo(1);
        assertThat(members.get(0).getUsername()).isEqualTo(usernameParam);
    }

    @Test
    void dynamicQuery_whereParam() {
        String usernameParam = "member1";
        Integer ageParam = 10;

        List<Member> members = searchMember2(usernameParam, ageParam);
        assertThat(members.size()).isEqualTo(1);

        assertThat(members.get(0).getUsername()).isEqualTo(usernameParam);
        assertThat(members.get(0).getAge()).isEqualTo(ageParam);
    }

    @Test
    void dynamicQuery_whereParam2() {
        List<Member> members = searchMember2(null, null);
        //null이면 무시됨.
        //모두 null이면, where 문 자체가 없이짐 -> 모두 검색
        assertThat(members.size()).isEqualTo(4);
    }

    @Test
    void dynamicQuery_whereParam3() {
        String usernameParam = "member1";

        List<Member> members = searchMember3(usernameParam, null);
        assertThat(members.size()).isEqualTo(1);
    }

    private List<Member> searchMember2(String usernameParam, Integer ageParam) {
        return query.selectFrom(member)
                .where(usernameEq(usernameParam), ageEq(ageParam))
                .fetch();
    }

    private List<Member> searchMember3(String usernameParam, Integer ageParam) {
        return query.selectFrom(member)
                .where(allEq(usernameParam, ageParam))
                .fetch();
    }

    private BooleanExpression allEq(String usernameParam, Integer ageParam) {
        return usernameEq(usernameParam).and(ageEq(ageParam));
    }

    private BooleanExpression usernameEq(String usernameParam) {
        if (usernameParam != null) {
            return member.username.eq(usernameParam);
        }
        return null;
    }

    private BooleanExpression ageEq(Integer ageParam) {
        if (ageParam != null) {
            return member.age.eq(ageParam);
        }
        return null;
    }

    private List<Member> searchMember1(String usernameParam, Integer ageParam) {
        BooleanBuilder builder = new BooleanBuilder();
        if (usernameParam != null) {
            builder.and(member.username.eq(usernameParam));
        }

        if (ageParam != null) {
            builder.and(member.age.eq(ageParam));
        }

        return query
                .selectFrom(member)
                .where(builder)
                .fetch();
    }
}

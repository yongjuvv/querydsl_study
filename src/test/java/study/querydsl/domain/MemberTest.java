package study.querydsl.domain;

import com.querydsl.core.NonUniqueResultException;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.condition.NoneNestedConditions;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.dto.MemberDto;
import study.querydsl.dto.QMemberDto;
import study.querydsl.dto.UserDto;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.Team;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static study.querydsl.entity.QMember.*;

@SpringBootTest
@Transactional
public class MemberTest {

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
    void startJPQL() {
        //given
        Member findMember = em.createQuery("select m from Member m where m.username = :username", Member.class)
                .setParameter("username", "member1")
                .getSingleResult();

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    void startQuerydsl() {
        Member findMember = query.select(member)
                .from(member)
                .where(member.username.eq("member1"))
                .fetchOne();
        assertThat(findMember.getUsername()).isEqualTo("member1");

    }

    @Test
    void search1() {
        Member findMember = query.selectFrom(member)
                .where(member.username.eq("member1")
                        .and(member.age.eq(10)))
                .fetchOne();
        assertThat(findMember.getUsername()).isEqualTo("member1");
        assertThat(findMember.getAge()).isEqualTo(10);
    }

    @Test
    void searchAndParam() {

        Member findMember = query.selectFrom(member)
                .where(member.username.eq("member1"), member.age.eq(10))
                .fetchOne();

        assertThat(findMember.getUsername()).isEqualTo("member1");
        assertThat(findMember.getAge()).isEqualTo(10);
    }

    @Test
    void resultFetch() {
        //다수 조회
        List<Member> members = query.selectFrom(member)
                .fetch();

        assertThat(members).hasSize(4);

        //데이터가 없으면 빈 리스트 반환
        List<Member> fetch = query.selectFrom(member)
                .where(member.username.eq("test"))
                .fetch();
        assertThat(fetch).isEmpty();


        //단건 조회
        //결과가 둘 이상인 경우 NonUniqueResultException 발생
        assertThatThrownBy(() -> query.selectFrom(member).fetchOne()).isInstanceOf(NonUniqueResultException.class);

        //처음 한 건 조회
        Member fetchFirst = query.selectFrom(member)
                .fetchFirst();
        assertThat(fetchFirst.getUsername()).isEqualTo("member1");
    }

    /**
     * 회원 정렬 순서
     * 1. 회원 나이 내림차순
     * 2. 회원 이름 오름차순
     * 3. 회원 이름이 없으면 마지막에 출력(null last)
     */
    @Test
    void sort() {
        em.persist(new Member(null, 100));
        em.persist(new Member("member5", 100));
        em.persist(new Member("member6", 100));

        List<Member> fetch = query.
                selectFrom(member)
                .where(member.age.eq(100))
                .orderBy(member.age.desc(), member.username.asc().nullsLast())
                .fetch();

        assertThat(fetch.get(0).getUsername()).isEqualTo("member5");
        assertThat(fetch.get(1).getUsername()).isEqualTo("member6");
        assertThat(fetch.get(2).getUsername()).isNull();
    }

    @Test
    void baseCase() {
        List<Tuple> result = query
                .select(member.username, member.age
                        .when(10).then("열살")
                        .when(20).then("스무살")
                        .otherwise("기타"))
                .from(member)
                .fetch();

        for (Tuple tuple : result) {
            System.out.println(tuple);
        }
    }

    @Test
    void concatTest() {
        List<String> result = query
                .select(member.username.concat("_").concat(member.age.stringValue()))
                .from(member)
                .fetch();

        for (String s : result) {
            System.out.println(s);
        }
    }




}

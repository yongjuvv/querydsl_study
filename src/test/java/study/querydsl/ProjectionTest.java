package study.querydsl;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.dto.MemberDto;
import study.querydsl.dto.QMemberDto;
import study.querydsl.dto.UserDto;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.Team;

import java.util.List;

import static study.querydsl.entity.QMember.member;

@SpringBootTest
@Transactional
public class ProjectionTest {
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
    void projectionOne() {
        List<String> result = query.select(member.username)
                .from(member)
                .fetch();

        for (String s : result) {
            System.out.println(s);
        }
    }

    @Test
    void projectionTwo() {
        List<Tuple> result = query
                .select(member.username, member.age)
                .from(member)
                .fetch();

        for (Tuple tuple : result) {
            System.out.println(tuple.get(member.username));
            System.out.println(tuple.get(member.age));

        }
    }

    @Test
    void projectionWithJpa() {
        List<MemberDto> result = em.createQuery(
                        "select new study.querydsl.dto.MemberDto(m.username, m.age)" +
                                "from Member m", MemberDto.class
                )
                .getResultList();

        for (MemberDto memberDto : result) {
            System.out.println(memberDto.toString());
        }
    }


    @Test
    void findDtoBySetter() {
        List<MemberDto> result = query
                .select(Projections.bean(MemberDto.class, member.username, member.age))
                .from(member)
                .fetch();

        for (MemberDto memberDto : result) {
            System.out.println(memberDto.toString());
        }
    }

    @Test
    void findDtoByField() {
        List<MemberDto> fetch = query
                .select(Projections.fields(MemberDto.class,
                        member.username,
                        member.age))
                .from(member)
                .fetch();

        for (MemberDto memberDto : fetch) {
            System.out.println(memberDto.toString());
        }
    }

    @Test
    void findUserDto() {
        List<UserDto> result = query
                .select(Projections.fields(UserDto.class,
                        member.username.as("name"),
                        member.age))
                .from(member)
                .fetch();

        for (UserDto userDto : result) {
            System.out.println(userDto.toString());
        }
    }

    @Test
    void findUserDtoBySubQuery() {
        QMember memberSub = new QMember("memberSub");

        List<UserDto> result = query
                .select(Projections.fields(UserDto.class,
                        member.username.as("name"),
                        ExpressionUtils.as(
                                JPAExpressions
                                        .select(memberSub.age.max())
                                        .from(memberSub), "age"
                        )))
                .from(member)
                .fetch();

        for (UserDto userDto : result) {
            System.out.println(userDto.toString());
        }
    }

    @Test
    void findDtoByConstructor() {
        List<MemberDto> result = query
                .select(Projections.constructor(MemberDto.class, member.username, member.age))
                .from(member)
                .fetch();

        for (MemberDto memberDto : result) {
            System.out.println(memberDto.toString());
        }
    }

    @Test
    void findDtoByQueryProjection() {
        List<MemberDto> result = query
                .select(new QMemberDto(member.username, member.age))
                .from(member)
                .fetch();

        for (MemberDto memberDto : result) {
            System.out.println(memberDto.toString());
        }
    }
}

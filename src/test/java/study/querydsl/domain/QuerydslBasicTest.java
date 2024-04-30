package study.querydsl.domain;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.QTeam;
import study.querydsl.entity.Team;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static study.querydsl.entity.QMember.*;
import static study.querydsl.entity.QTeam.*;

@Transactional
@SpringBootTest
public class QuerydslBasicTest {

    @Autowired
    EntityManager em;

    JPAQueryFactory query;

    @BeforeEach
    void beforeEach() {
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
    }

    @Test
    void paging1() {
        List<Member> result = query.selectFrom(member)
                .orderBy(member.username.desc())
                .offset(1) //몇 개 건너뛸건지
                .limit(2) //몇 개 뽑아낼건지
                .fetch();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getUsername()).isEqualTo("member3");
        assertThat(result.get(1).getUsername()).isEqualTo("member2");
    }


    @Test
    void aggregation() {
        List<Tuple> result = query.select(
                        member.count(), member.age.sum(), member.age.max(), member.age.min(), member.age.avg()
                ).from(member)
                .fetch();

        Tuple tuple = result.get(0);
        Long memberCnt = tuple.get(member.count());
        Double ageAvg = tuple.get(member.age.avg());
        Integer ageMax = tuple.get(member.age.max());
        Integer ageMin = tuple.get(member.age.min());
        Integer ageSum = tuple.get(member.age.sum());

        assertThat(memberCnt).isEqualTo(4);
        assertThat(ageAvg).isEqualTo(25);
        assertThat(ageMax).isEqualTo(40);
        assertThat(ageMin).isEqualTo(10);
        assertThat(ageSum).isEqualTo(100);
    }

    /**
     * 팀의 이름과 각 팀의 평균 연령 구하기
     */
    @Test
    void aggregation2() {
        List<Tuple> result = query.select(team.name, member.age.avg())
                .from(member)
                .join(member.team, team)
                .groupBy(team.name)
                .fetch();

        Tuple teamA = result.get(0);
        Tuple teamB = result.get(1);

        assertThat(teamA.get(team.name)).isEqualTo("TeamA");
        assertThat(teamA.get(member.age.avg())).isEqualTo(15);
        assertThat(teamB.get(team.name)).isEqualTo("TeamB");
        assertThat(teamB.get(member.age.avg())).isEqualTo(35);
    }

    /**
     * 팀의 이름과 각 팀의 평균 연령을 구하고, 평균 연령이 20 이상인 경우만 가져오기
     */
    @Test
    void aggregation3() {
        List<Tuple> result = query.select(team.name, member.age.avg())
                .from(member)
                .join(member.team, team)
                .groupBy(team.name)
                .having(member.age.avg().gt(20))
                .fetch();

        Tuple teamB = result.get(0);

        assertThat(teamB.get(team.name)).isEqualTo("TeamB");
        assertThat(teamB.get(member.age.avg())).isEqualTo(35);
    }

    @Test
    void join() {
        List<Member> fetch = query.selectFrom(member)
                .join(member.team, team)
                .where(team.name.eq("TeamA"))
                .fetch();


        assertThat(fetch).extracting("username").containsExactly("member1", "member2");
    }

    /**
     * 회원과 팀을 조인하면서, 팀 이름이 TeamA인 팀만 조인하고, 회원은 leftJoin을 통해 모두 조회한다.
     */
    @Test
    void joinOnFiltering() {
        List<Tuple> result = query.select(member, team)
                .from(member)
                .leftJoin(member.team, team)
                .on(team.name.eq("TeamA"))
                .fetch();

        for (Tuple tuple : result) {
            System.out.println(tuple);
        }
    }

    /**
     * 연관관계가 없는 조인
     * 회원의 이름이 팀 이름과 같은 멤버 조회
     * left join을 통해 모든 회원 조회
     */
    @Test
    void joinOnNoRelation() {
        em.persist(new Member("TeamA"));
        em.persist(new Member("TeamB"));
        em.persist(new Member("TeamC"));

        List<Tuple> fetch = query.select(member, team)
                .from(member)
                .leftJoin(team).on(member.username.eq(team.name))
                .fetch();

        for (Tuple tuple : fetch) {
            System.out.println(tuple);
        }

    }

    @Test
    void thetaJoin() {
        //given
        em.persist(new Member("TeamA"));
        em.persist(new Member("TeamB"));

        List<Member> result = query
                .select(member)
                .from(member, team)
                .where(member.username.eq(team.name))
                .fetch();

        assertThat(result).extracting("username").containsExactly("TeamA", "TeamB");
    }

    @Test
    void subQuery() {
        em.persist(new Member("max_age_member", 100));
        QMember qMember = new QMember("q");

        Member findMember = query
                .selectFrom(member)
                .where(member.age.eq(
                        JPAExpressions
                                .select(qMember.age.max())
                                .from(qMember)
                ))
                .fetchOne();

        assertThat(findMember.getAge()).isEqualTo(100);
    }

    /**
     * 회원 중, 나이가 평균 이상인 회원 조회
     */
    @Test
    void subQueryAvg() {
        QMember qMember = new QMember("q");

        List<Member> members = query
                .selectFrom(member)
                .where(member.age.goe(
                        JPAExpressions
                                .select(qMember.age.avg())
                                .from(qMember)
                ))
                .fetch();

        for (Member member1 : members) {
            System.out.println(member1.getAge());
        }


        assertThat(members.size()).isEqualTo(2);
    }

}

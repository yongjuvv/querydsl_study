package study.querydsl.repository;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.entity.Member;
import study.querydsl.entity.Team;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class MemberRepositoryTest {

    @Autowired
    MemberRepository repository;

    @Autowired
    EntityManager em;

    @Test
    void searchTest() {
        Team team1 = new Team("teamA");
        Team team2 = new Team("teamB");

        em.persist(team1);
        em.persist(team2);


        Member member1 = new Member("memberA", 10, team1);
        Member member2 = new Member("memberB", 20, team1);
        Member member3 = new Member("memberC", 30, team2);
        Member member4 = new Member("memberD", 40, team2);

        repository.save(member1);
        repository.save(member2);
        repository.save(member3);
        repository.save(member4);

        MemberSearchCondition condition = new MemberSearchCondition();
        condition.setTeamName("teamA");
        condition.setAgeGoe(10);
        condition.setAgeLoe(30);
        //10 ~ 30살 사이인 teamA의 팀원 구하기

        List<MemberTeamDto> result = repository.search(condition);
        for (MemberTeamDto memberTeamDto : result) {
            System.out.println(memberTeamDto.toString());
        }

        assertThat(result).hasSize(2);
        assertThat(result).extracting("teamName").containsOnly("teamA");
    }

    @Test
    void findUsernameTest() {
        Member member1 = new Member("memberA", 10);
        Member member2 = new Member("memberB", 20);
        Member member3 = new Member("memberC", 30);
        Member member4 = new Member("memberD", 40);

        repository.save(member1);
        repository.save(member2);
        repository.save(member3);
        repository.save(member4);

        List<Member> findMember = repository.findByUsername("memberA");

        assertThat(findMember).containsExactly(member1);
        assertThat(findMember).extracting("username").containsOnly("memberA");

    }

    @Test
    void searchPageTest1() {
        Team team1 = new Team("teamA");
        Team team2 = new Team("teamB");

        em.persist(team1);
        em.persist(team2);

        Member member1 = new Member("memberA", 10, team1);
        Member member2 = new Member("memberB", 20, team1);
        Member member3 = new Member("memberC", 30, team2);
        Member member4 = new Member("memberD", 40, team2);
        Member member5 = new Member("memberD", 40, team2);

        repository.save(member1);
        repository.save(member2);
        repository.save(member3);
        repository.save(member4);
        repository.save(member5);

        MemberSearchCondition condition = new MemberSearchCondition();
        PageRequest pageRequest = PageRequest.of(0, 3);
        //0번째 페이지(첫 번째 페이지), 한 페이지에 담을 수 있는 content의 개수는 3개임 -> 3개의 콘텐츠가 나옴

        Page<MemberTeamDto> result = repository.searchPageSimple(condition, pageRequest);

        assertThat(result).hasSize(3);
    }

    @Test
    void searchPageTest2() {
        Team team1 = new Team("teamA");
        Team team2 = new Team("teamB");

        em.persist(team1);
        em.persist(team2);

        Member member1 = new Member("memberA", 10, team1);
        Member member2 = new Member("memberB", 20, team1);
        Member member3 = new Member("memberC", 30, team2);
        Member member4 = new Member("memberD", 40, team2);
        Member member5 = new Member("memberD", 40, team2);

        repository.save(member1);
        repository.save(member2);
        repository.save(member3);
        repository.save(member4);
        repository.save(member5);

        MemberSearchCondition condition = new MemberSearchCondition();
        PageRequest pageRequest = PageRequest.of(0, 100);

        Page<MemberTeamDto> result = repository.searchPageSimple(condition, pageRequest);

        assertThat(result).hasSize(5);
    }


}
package study.querydsl.repository;

import jakarta.persistence.EntityManager;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.entity.Member;
import study.querydsl.entity.Team;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class MemberJpaRepositoryV2Test {

    @Autowired
    EntityManager em;

    @Autowired
    MemberJpaRepositoryV2 repository;

    @Test
    void findAllTest() {
        //given
        Member member1 = new Member("memberA", 10);
        Member member2 = new Member("memberB", 20);
        Member member3 = new Member("memberC", 30);
        Member member4 = new Member("memberD", 40);

        repository.save(member1);
        repository.save(member2);
        repository.save(member3);
        repository.save(member4);
        //when
        List<Member> members = repository.findAll_Querydsl();
        //then
        assertThat(members).hasSize(4);
        assertThat(members).contains(member1, member2, member3, member4);
    }

    @Test
    void findByUsernameTest() {
        //given
        Member member1 = new Member("memberA", 10);
        Member member2 = new Member("memberB", 20);
        Member member3 = new Member("memberC", 30);
        Member member4 = new Member("memberD", 40);

        repository.save(member1);
        repository.save(member2);
        repository.save(member3);
        repository.save(member4);

        //when
        List<Member> result = repository.findByUsername("memberC");

        //then
        assertThat(result).hasSize(1);
        assertThat(result).containsExactly(member3);
        assertThat(result).extracting("username").containsOnly("memberC");
    }


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

        List<MemberTeamDto> result = repository.searchByBuilder(condition);
        for (MemberTeamDto memberTeamDto : result) {
            System.out.println(memberTeamDto.toString());
        }

        assertThat(result).hasSize(2);
        assertThat(result).extracting("teamName").containsOnly("teamA");
    }



}
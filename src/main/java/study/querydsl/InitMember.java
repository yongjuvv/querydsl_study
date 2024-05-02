package study.querydsl;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.entity.Member;
import study.querydsl.entity.Team;

@Profile("local")
@Component
@RequiredArgsConstructor
public class InitMember {

    private final InitMemberService service;

    @PostConstruct
    public void init() {
        service.init();
    }


    @Component
    static class InitMemberService {
        @PersistenceContext
        private EntityManager em;

        @Transactional
        public void init() {
            Team teamA = new Team("teamA");
            Team teamB = new Team("teamB");

            em.persist(teamA);
            em.persist(teamB);

            for(int i = 0 ; i < 100 ; i++) {
                if(i % 2 == 0) {
                    em.persist(new Member("member" + i, i, teamA));
                }else {
                    em.persist(new Member("member" + i, i, teamB));
                }
            }
        }
    }
}

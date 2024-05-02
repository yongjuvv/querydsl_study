package study.querydsl.repository;

import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;
import study.querydsl.entity.Member;

import java.util.List;
import java.util.Optional;

/**
 * 순수 JPA 사용
 */
@Repository
public class MemberJpaRepositoryV1 {
    private final EntityManager em;

    public MemberJpaRepositoryV1(EntityManager em) {
        this.em = em;
    }

    public void save(Member member) {
        em.persist(member);
    }

    public Optional<Member> findById(Long id) {
        Member member = em.find(Member.class, id);
        return Optional.ofNullable(member);
    }

    public List<Member> findAll() {
        return em.createQuery("select m from Member m", Member.class).getResultList();
    }

    public List<Member> findByUsername(String username) {
        return em.createQuery("select m from Member m where m.username =: username", Member.class)
                .setParameter("username", username)
                .getResultList();
    }
}

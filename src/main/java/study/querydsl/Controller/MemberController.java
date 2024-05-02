package study.querydsl.Controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RestController;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.repository.MemberJpaRepositoryV3;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberJpaRepositoryV3 repository;

    @GetMapping("v1/members")
    public List<MemberTeamDto> searchMemberV1(@ModelAttribute MemberSearchCondition condition) {
        return repository.searchByBuilder(condition);
    }
}

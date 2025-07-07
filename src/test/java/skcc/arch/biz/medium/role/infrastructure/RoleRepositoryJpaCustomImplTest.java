package skcc.arch.biz.medium.role.infrastructure;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import skcc.arch.biz.role.domain.Role;
import skcc.arch.biz.role.infrastructure.RoleRepositoryJpaCustomImpl;
import skcc.arch.biz.role.infrastructure.jpa.RoleRepositoryJpa;

import java.util.Optional;


@ExtendWith(SpringExtension.class)
@DataJpaTest
@EnableJpaAuditing
@Slf4j
class RoleRepositoryJpaCustomImplTest {


    @Autowired
    private RoleRepositoryJpa repository;

    @Autowired
    private EntityManager entityManager;

    private RoleRepositoryJpaCustomImpl repositoryCustomImpl;

    @BeforeEach
    void setUp() {
        JPAQueryFactory jpaQueryFactory = new JPAQueryFactory(entityManager);
        repositoryCustomImpl = new RoleRepositoryJpaCustomImpl(repository, jpaQueryFactory);
    }

    @Test
    public void findById_존재하지_않는_데이터() throws Exception{
        //given
        Long id = 1L;

        //when
        Optional<Role> result = repositoryCustomImpl.findById(id);

        //then
        Assertions.assertThat(result.isEmpty()).isTrue();
    }
}
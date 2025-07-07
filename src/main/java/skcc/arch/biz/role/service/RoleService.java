package skcc.arch.biz.role.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import skcc.arch.app.exception.CustomException;
import skcc.arch.app.exception.ErrorCode;
import skcc.arch.biz.role.controller.port.RoleServicePort;
import skcc.arch.biz.role.domain.Role;
import skcc.arch.biz.role.service.port.RoleRepositoryPort;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RoleService implements RoleServicePort {

    private final RoleRepositoryPort repository;

    @Override
    public Role save(Role role) {

        validationExistRoleId(role);

        return repository.save(role);
    }

    @Override
    public Role findById(Long id) {
        return repository.findById(id).orElseThrow(
                () -> new CustomException(ErrorCode.NOT_FOUND_ELEMENT));
    }

    @Override
    public Page<Role> findByCondition(Pageable pageable, Role search) {
        return repository.findByCondition(pageable, search);
    }

    @Override
    @Transactional
    public Role update(Role param) {

        // ID가 존재하는 요소인지?
        Role dbData = repository.findById(param.getId()).orElseThrow(
                () -> new CustomException(ErrorCode.NOT_FOUND_ELEMENT));

        // ROLE_ID가 중복된건 아닌지?
        if( !dbData.getRoleId().equals(param.getRoleId())) {
            validationExistRoleId(param);
        }

        Role target = Role.update(param, dbData);

        return repository.save(target);
    }

    private void validationExistRoleId(Role param) {
        Optional<Role> existingRole = repository.findByRoleId(param.getRoleId());
        if (existingRole.isPresent()) {
            throw new CustomException(ErrorCode.EXIST_ELEMENT);
        }
    }
}

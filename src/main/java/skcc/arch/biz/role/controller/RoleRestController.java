package skcc.arch.biz.role.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import skcc.arch.app.dto.ApiResponse;
import skcc.arch.app.dto.PageInfo;
import skcc.arch.biz.role.controller.port.RoleServicePort;
import skcc.arch.biz.role.controller.request.RoleCreateRequest;
import skcc.arch.biz.role.controller.request.RoleSearchRequest;
import skcc.arch.biz.role.controller.request.RoleUpdateRequest;
import skcc.arch.biz.role.controller.response.RoleResponse;
import skcc.arch.biz.role.domain.Role;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/role")
public class RoleRestController {

    private final RoleServicePort roleServicePort;

    @PostMapping
    public ApiResponse<RoleResponse> createRole(@RequestBody @Valid RoleCreateRequest role) {
        return ApiResponse.ok(RoleResponse.from(roleServicePort.save(role.toModel())));

    }

    @GetMapping("/{roleId}")
    public ApiResponse<RoleResponse> getRole(@PathVariable Long roleId) {
        return ApiResponse.ok(RoleResponse.from(roleServicePort.findById(roleId)));
    }

    @GetMapping
    public ApiResponse<List<RoleResponse>> getRoleListByCondition(Pageable pageable, RoleSearchRequest role) {

        Page<Role> result = roleServicePort.findByCondition(pageable, role.toModel());
        List<RoleResponse> content = result.getContent().stream().map(RoleResponse::from).toList();
        return ApiResponse.ok(content, PageInfo.fromPage(result));
    }

    @PatchMapping
    public ApiResponse<RoleResponse> updateRole(@RequestBody RoleUpdateRequest role) {
        return ApiResponse.ok(RoleResponse.from(roleServicePort.update(role.toModel())));
    }

}

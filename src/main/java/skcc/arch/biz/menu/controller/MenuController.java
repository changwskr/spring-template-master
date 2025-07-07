package skcc.arch.biz.menu.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import skcc.arch.app.dto.ApiResponse;
import skcc.arch.app.dto.PageInfo;
import skcc.arch.biz.menu.controller.port.MenuServicePort;
import skcc.arch.biz.menu.controller.request.MenuCreateRequest;
import skcc.arch.biz.menu.controller.request.MenuSearchRequest;
import skcc.arch.biz.menu.controller.request.MenuUpdateRequest;
import skcc.arch.biz.menu.controller.response.MenuResponse;
import skcc.arch.biz.menu.domain.Menu;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/menu")
public class MenuController {

    private final MenuServicePort menuServicePort;

    @PostMapping
    public ApiResponse<MenuResponse> createMenu(@RequestBody @Valid MenuCreateRequest request) {
        return ApiResponse.ok(MenuResponse.from(menuServicePort.save(request.toModel())));
    }

    @GetMapping("/{menuId}")
    public ApiResponse<MenuResponse> getMenu(@PathVariable Long menuId) {
        return ApiResponse.ok(MenuResponse.from(menuServicePort.findByMenuId(menuId)));
    }

    @GetMapping
    public ApiResponse<List<MenuResponse>> getMenuListByCondition(Pageable pageable, MenuSearchRequest menuSearchRequest) {

        Page<Menu> result = menuServicePort.findByCondition(pageable, menuSearchRequest.toModel());
        return ApiResponse.ok(
                result.getContent()
                        .stream()
                        .map(MenuResponse::from)
                        .toList(),
                PageInfo.fromPage(result)
        );
    }

    @PatchMapping
    public ApiResponse<MenuResponse> updateMenu(@RequestBody @Valid MenuUpdateRequest menuUpdateRequest) {
        return ApiResponse.ok(MenuResponse.from(menuServicePort.update(menuUpdateRequest.toModel())));
    }

    @GetMapping("/root")
    public ApiResponse<List<MenuResponse>> getRootMenuList(@RequestParam(required = false, defaultValue = "false") boolean dbSelect) {
        return ApiResponse.ok(
                menuServicePort.getRootMenuList(dbSelect)
                        .stream()
                        .map(MenuResponse::from)
                        .toList()
        );
    }
}

package skcc.arch.biz.code.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import skcc.arch.biz.code.controller.port.CodeServicePort;
import skcc.arch.biz.code.controller.request.CodeCreateRequest;
import skcc.arch.biz.code.controller.request.CodeSearchRequest;
import skcc.arch.biz.code.controller.request.CodeUpdateRequest;
import skcc.arch.biz.code.domain.Code;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/code")
@RequiredArgsConstructor
@Slf4j
public class CodeManagementController {

    private final CodeServicePort codeServicePort;

    @GetMapping
    public String codeMain() {
        return "redirect:/code/list";
    }

    @GetMapping("/list")
    public String codeList(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String description,
            @RequestParam(required = false, defaultValue = "false") Boolean includeDeleted,
            @PageableDefault(size = 20) Pageable pageable,
            Model model) {
        
        CodeSearchRequest searchRequest = CodeSearchRequest.builder()
                .code(code)
                .codeName(name)
                .description(description)
                .delYn(includeDeleted ? null : false) // 삭제된 코드 포함 여부에 따라 필터링
                .build();
        
        Page<Code> result = codeServicePort.findByCode(pageable, searchRequest.toModel());
        
        model.addAttribute("codes", result.getContent());
        model.addAttribute("page", result);
        model.addAttribute("searchRequest", searchRequest);
        
        return "code/list";
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        CodeCreateRequest codeCreateRequest = CodeCreateRequest.builder()
                .code("")
                .codeName("")
                .description("")
                .seq(0)
                .build();
        model.addAttribute("code", codeCreateRequest);
        model.addAttribute("isEdit", false);
        return "code/form";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute CodeCreateRequest codeCreateRequest, Model model) {
        try {
            codeServicePort.save(codeCreateRequest.toModel());
            return "redirect:/code/list";
        } catch (Exception e) {
            log.error("코드 등록 중 오류 발생: {}", e.getMessage());
            model.addAttribute("error", "코드 등록 중 오류가 발생했습니다.");
            model.addAttribute("code", codeCreateRequest);
            model.addAttribute("isEdit", false);
            return "code/form";
        }
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        try {
            Code code = codeServicePort.findById(id);
            CodeUpdateRequest codeUpdateRequest = CodeUpdateRequest.builder()
                    .id(code.getId())
                    .code(code.getCode())
                    .codeName(code.getCodeName())
                    .description(code.getDescription())
                    .seq(code.getSeq())
                    .delYn(code.isDelYn())
                    .build();
            model.addAttribute("code", codeUpdateRequest);
            model.addAttribute("isEdit", true);
            return "code/form";
        } catch (Exception e) {
            log.error("코드 조회 중 오류 발생: {}", e.getMessage());
            return "redirect:/code/list";
        }
    }

    @PostMapping("/update")
    public String update(@ModelAttribute CodeUpdateRequest codeUpdateRequest, Model model) {
        try {
            codeServicePort.update(codeUpdateRequest.toModel());
            return "redirect:/code/list";
        } catch (Exception e) {
            log.error("코드 수정 중 오류 발생: {}", e.getMessage());
            model.addAttribute("error", "코드 수정 중 오류가 발생했습니다.");
            model.addAttribute("code", codeUpdateRequest);
            model.addAttribute("isEdit", true);
            return "code/form";
        }
    }

    @RequestMapping(value = "/delete/{id}", method = {RequestMethod.GET, RequestMethod.POST})
    public String delete(@PathVariable Long id, HttpServletRequest request) {
        log.info("★★★ DELETE 컨트롤러 진입 - ID: {}, Method: {}, URI: {}", id, request.getMethod(), request.getRequestURI());
        try {
            log.info("코드 삭제 요청 - ID: {}", id);
            codeServicePort.delete(id);
            log.info("코드 삭제 성공 - ID: {}", id);
            return "redirect:/code/list?success=deleted";
        } catch (Exception e) {
            log.error("코드 삭제 실패 - ID: {}, 오류: {}", id, e.getMessage(), e);
            return "redirect:/code/list?error=delete_failed";
        }
    }
} 
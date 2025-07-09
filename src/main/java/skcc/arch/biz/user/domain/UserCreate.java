package skcc.arch.biz.user.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Builder
public class UserCreate {

    private final String email;
    private final String password;
    private final String username;
    private final String userId;        // 요구사항: USERID
    private final String address;       // 요구사항: 주소
    private final String job;           // 요구사항: 직업
    private final Integer age;          // 요구사항: 나이
    private final String company;       // 요구사항: 회사

}

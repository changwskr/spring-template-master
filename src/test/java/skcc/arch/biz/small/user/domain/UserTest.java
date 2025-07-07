package skcc.arch.biz.small.user.domain;


import org.junit.jupiter.api.Test;
import skcc.arch.biz.mock.FakePasswordEncoder;
import skcc.arch.biz.role.domain.Role;
import skcc.arch.biz.user.controller.request.UserUpdateRequest;
import skcc.arch.biz.user.domain.User;
import skcc.arch.biz.user.domain.UserCreate;
import skcc.arch.biz.user.domain.UserStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserTest {

    @Test
    public void UserCreate_객체로_생성() throws Exception {
        //given
        UserCreate userCreate = UserCreate.builder()
                .username("홍길동")
                .email("email@sk.com")
                .password("password123")
                .build();

        //when
        User user = User.from(userCreate, new FakePasswordEncoder());

        //then
        assertThat(user.getId()).isNull();
        assertThat(user.getUsername()).isEqualTo(userCreate.getUsername());
        assertThat(user.getEmail()).isEqualTo(userCreate.getEmail());
        assertThat(user.getPassword()).isEqualTo("ENC_" +userCreate.getPassword());
        assertThat(user.getStatus()).isEqualTo(UserStatus.PENDING);
        assertThat(user.getCreatedDate()).isNotNull();
        assertThat(user.getLastModifiedDate()).isNotNull();
        assertThat(user.getCreatedDate()).isBeforeOrEqualTo(user.getLastModifiedDate());
        assertThat(user.getLastModifiedDate()).isAfterOrEqualTo(user.getCreatedDate());
        
    }

    @Test
    void 사용자의_상태값을_변경한다_상태값이_다른경우() throws Exception {
        //given
        UserUpdateRequest userUpdateRequest = UserUpdateRequest.builder()
                .status(UserStatus.ACTIVE)
                .build();
        String userName = "홍길동";
        User originalUser = User.builder()
                .username(userName)
                .status(UserStatus.PENDING)
                .build();

        //when
        User updated = originalUser.updateStatus(userUpdateRequest.getStatus());

        //then
        assertThat(updated.getUsername()).isEqualTo(userName);
        assertThat(updated.getStatus()).isEqualTo(userUpdateRequest.getStatus());
    }

    @Test
    void 사용자의_상태값을_변경한다_상태값이_같은경우() throws Exception {
        //given
        UserUpdateRequest userUpdateRequest = UserUpdateRequest.builder()
                .status(UserStatus.ACTIVE)
                .build();
        String userName = "홍길동";
        User originalUser = User.builder()
                .username(userName)
                .status(UserStatus.ACTIVE)
                .build();

        //when & then
        assertThrows (IllegalStateException.class, () -> originalUser.updateStatus(userUpdateRequest.getStatus()));

    }

    @Test
    void 사용자_정보중_일부만_변경() throws Exception {
        //given
        // 사용자
        String userName = "홍길동";
        User originalUser = User.builder()
                .username(userName)
                .status(UserStatus.PENDING)
                .build();
        // 사용자권한
        Role role = Role.builder().roleId("NO_CHANGE").build();
        originalUser.addUserRole(role);

        // 변경대상
        String newUserName = "홍길순";
        UserUpdateRequest userUpdateRequest = UserUpdateRequest.builder()
                .username(newUserName)
                .build();

        //when
        User updatedUser = originalUser.updateUser(userUpdateRequest.toModel(), new FakePasswordEncoder());

        //then
        assertThat(updatedUser.getUsername()).isEqualTo(userUpdateRequest.getUsername());
        assertThat(updatedUser.getStatus()).isEqualTo(originalUser.getStatus());
        assertThat(updatedUser.getUserRoles().size()).isEqualTo(1);
        assertThat(updatedUser.getUserRoles().get(0).getRole().getRoleId()).isEqualTo("NO_CHANGE");
    }

    @Test
    public void 사용자_생성시_기본권한이_부여된다() throws Exception{
        //given
        UserCreate userCreate = UserCreate.builder()
                .username("홍길동")
                .email("email@sk.com")
                .password("password123")
                .build();

        //when
        User user = User.from(userCreate, new FakePasswordEncoder());

        //then
        assertThat(user.getUserRoles().size()).isEqualTo(1);
        assertThat(user.getUserRoles().get(0).getRole()).isEqualTo(Role.defaultRole());
    }

}
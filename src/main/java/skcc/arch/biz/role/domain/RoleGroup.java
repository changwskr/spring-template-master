package skcc.arch.biz.role.domain;

import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
public class RoleGroup {
    private final long id;
    private final String roleGroupId;
    private final String name;
    private final List<Role> roleList = new ArrayList<>();
    private final long createdDate;
    private final long lastModifiedDate;
}

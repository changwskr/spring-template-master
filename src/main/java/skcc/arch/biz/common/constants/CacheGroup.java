package skcc.arch.biz.common.constants;

public enum CacheGroup {
    CODE("code", "코드ID 기준으로 캐시 KEY를 설정한다."),
    MENU("menu", "계층형 정보를 Map형태로 저장")
    ;

    private String name;
    private String desc;

    CacheGroup(String name, String desc) {
        this.name = name;
        this.desc = desc;
    }

    public static CacheGroup getByName(String name) {
        return switch (name) {
            case "code" -> CODE;
            default -> null;
        };
    }
}

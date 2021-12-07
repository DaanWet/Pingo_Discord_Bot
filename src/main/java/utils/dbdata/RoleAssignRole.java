package utils.dbdata;

public class RoleAssignRole {

    private String name;
    private String emoji;
    private Long roleId;

    public RoleAssignRole(String emoji, String name, Long roleId) {
        this.name = name;
        this.emoji = emoji;
        this.roleId = roleId;
    }

    public String getName() {
        return name;
    }

    public String getEmoji() {
        return emoji;
    }

    public Long getRoleId() {
        return roleId;
    }
}

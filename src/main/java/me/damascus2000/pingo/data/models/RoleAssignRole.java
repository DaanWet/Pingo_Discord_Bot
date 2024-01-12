package me.damascus2000.pingo.data.models;

@Deprecated(forRemoval = true)
public class RoleAssignRole {

    private final String name;
    private final String emoji;
    private final Long roleId;

    public RoleAssignRole(String emoji, String name, Long roleId){
        this.name = name;
        this.emoji = emoji;
        this.roleId = roleId;
    }

    public String getName(){
        return name;
    }

    public String getEmoji(){
        return emoji;
    }

    public Long getRoleId(){
        return roleId;
    }
}

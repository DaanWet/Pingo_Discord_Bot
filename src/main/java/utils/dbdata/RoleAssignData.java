package utils.dbdata;

import commands.roles.RoleCommand;

public class RoleAssignData {

    private Long channelId;
    private Long messageId;
    private RoleCommand.Compacting compacting;
    private RoleCommand.Sorting sorting;
    private String customS;
    private String title;

    public RoleAssignData(){};


    public RoleAssignData(Long channelId, Long messageId, RoleCommand.Compacting compacting, RoleCommand.Sorting sorting, String title){
        this.channelId = channelId;
        this.messageId = messageId;
        this.compacting = compacting;
        this.sorting = sorting;
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Long getChannelId() {
        return channelId;
    }

    public void setChannelId(Long channelId) {
        this.channelId = channelId;
    }

    public Long getMessageId() {
        return messageId;
    }

    public void setMessageId(Long messageId) {
        this.messageId = messageId;
    }

    public RoleCommand.Compacting getCompacting() {
        return compacting;
    }

    public void setCompacting(RoleCommand.Compacting compacting) {
        this.compacting = compacting;
    }

    public RoleCommand.Sorting getSorting() {
        return sorting;
    }

    public void setSorting(RoleCommand.Sorting sorting) {
        this.sorting = sorting;
    }

    public String getCustomS() {
        return customS;
    }

    public void setCustomS(String customS) {
        this.customS = customS;
    }
}

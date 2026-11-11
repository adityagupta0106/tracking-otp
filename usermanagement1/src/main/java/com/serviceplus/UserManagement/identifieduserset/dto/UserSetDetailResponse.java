package com.serviceplus.UserManagement.identifieduserset.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.List;

public class UserSetDetailResponse {

    private UserSetResponse userSet;
    @JsonIgnore
    private List<MemberResponse> members;

    public UserSetDetailResponse() {
    }

    public UserSetDetailResponse(UserSetResponse userSet, List<MemberResponse> members) {
        this.userSet = userSet;
        this.members = members;
    }

    public UserSetResponse getUserSet() {
        return userSet;
    }

    public void setUserSet(UserSetResponse userSet) {
        this.userSet = userSet;
    }

    public List<MemberResponse> getMembers() {
        return members;
    }

    public void setMembers(List<MemberResponse> members) {
        this.members = members;
    }
}

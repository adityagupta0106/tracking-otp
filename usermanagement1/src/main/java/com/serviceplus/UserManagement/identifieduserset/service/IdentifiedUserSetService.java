package com.serviceplus.UserManagement.identifieduserset.service;

import com.serviceplus.UserManagement.Dto.JobStatusDto;
import com.serviceplus.UserManagement.identifieduserset.dto.*;
import com.serviceplus.UserManagement.identifieduserset.entity.IdentifiedUserSetUploadJob;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IdentifiedUserSetService {

    UserSetResponse createUserSet(CreateUserSetRequest request, HttpServletRequest httpRequest);

    List<UserSetResponse> getUserSets(HttpServletRequest httpRequest);

    UserSetDetailResponse getUserSetDetail(Long id, HttpServletRequest httpRequest);

    UserSetResponse updateUserSet(Long id, UpdateUserSetRequest request, HttpServletRequest httpRequest);

    UserSetResponse changeUserSetStatus(Long id, HttpServletRequest httpRequest);

    MemberResponse addMember(Long userSetId, AddMemberRequest request, HttpServletRequest httpRequest);

    void removeMember(Long userSetId, Long userId, HttpServletRequest httpRequest);

    ExcelUploadJobResponse uploadMembersExcel(Long userSetId, MultipartFile file, HttpServletRequest httpRequest);

    ExcelUploadJobResponse exportMembersAsync(Long userSetId, HttpServletRequest httpRequest);

    byte[] exportMembersToExcel(Long userSetId, HttpServletRequest httpRequest);

    byte[] downloadTemplate();

    List<JobStatusDto> getUploadJobStatus(Long jobId, HttpServletRequest httpRequest);

    List<Integer> getUserSetIdsForUser(Long userId, String tenantId, HttpServletRequest httpRequest);
}

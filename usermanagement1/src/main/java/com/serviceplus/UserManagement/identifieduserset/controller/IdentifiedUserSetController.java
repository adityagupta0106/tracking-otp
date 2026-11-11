package com.serviceplus.UserManagement.identifieduserset.controller;

import com.serviceplus.UserManagement.Dto.JobStatusDto;
import com.serviceplus.UserManagement.identifieduserset.dto.*;
import com.serviceplus.UserManagement.identifieduserset.entity.IdentifiedUserSetUploadJob;
import com.serviceplus.UserManagement.identifieduserset.service.IdentifiedUserSetService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/a")
public class IdentifiedUserSetController {

    private final IdentifiedUserSetService identifiedUserSetService;

    @Autowired
    public IdentifiedUserSetController(IdentifiedUserSetService identifiedUserSetService) {
        this.identifiedUserSetService = identifiedUserSetService;
    }

    // --- RESTful endpoints using exclusively @GetMapping and @PostMapping with strictly @RequestParam only ---

    @PostMapping("/identified-user-sets")
    public ResponseEntity<UserSetResponse> createUserSet(@RequestBody CreateUserSetRequest request, HttpServletRequest httpRequest) {
        return ResponseEntity.ok(identifiedUserSetService.createUserSet(request, httpRequest));
    }

    @GetMapping("/identified-user-sets")
    public ResponseEntity<?> getUserSets(@RequestParam(required = false) Long id, HttpServletRequest httpRequest) {
        if (id != null) {
            return ResponseEntity.ok(identifiedUserSetService.getUserSetDetail(id, httpRequest));
        }
        return ResponseEntity.ok(identifiedUserSetService.getUserSets(httpRequest));
    }

    @PostMapping("/identified-user-sets/update")
    public ResponseEntity<UserSetResponse> updateUserSet(@RequestParam Long id, @RequestBody UpdateUserSetRequest request, HttpServletRequest httpRequest) {
        return ResponseEntity.ok(identifiedUserSetService.updateUserSet(id, request, httpRequest));
    }

    @PostMapping("/identified-user-sets/status")
    public ResponseEntity<UserSetResponse> changeUserSetStatus(@RequestParam Long id, HttpServletRequest httpRequest) {
        return ResponseEntity.ok(identifiedUserSetService.changeUserSetStatus(id, httpRequest));
    }

    @PostMapping("/identified-user-sets/members/add")
    public ResponseEntity<MemberResponse> addMemberAlt(@RequestParam Long userSetId, @RequestBody AddMemberRequest request, HttpServletRequest httpRequest) {
        return ResponseEntity.ok(identifiedUserSetService.addMember(userSetId, request, httpRequest));
    }

    @PostMapping(value = "/identified-user-sets/members/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ExcelUploadJobResponse> uploadMembersExcel(@RequestParam Long userSetId,
                                                                    @RequestParam("file") MultipartFile file,
                                                                    HttpServletRequest httpRequest) {
        return ResponseEntity.accepted().body(identifiedUserSetService.uploadMembersExcel(userSetId, file, httpRequest));
    }

    @PostMapping("/identified-user-sets/export")
    public ResponseEntity<ExcelUploadJobResponse> exportMembersAsyncPost(@RequestParam Long userSetId, HttpServletRequest httpRequest) {
        return ResponseEntity.accepted().body(identifiedUserSetService.exportMembersAsync(userSetId, httpRequest));
    }

    @GetMapping("/identified-user-sets/template")
    public ResponseEntity<byte[]> downloadTemplate() {
        byte[] excelBytes = identifiedUserSetService.downloadTemplate();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Identified_Users_Template.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelBytes);
    }

    @PostMapping("/identified-user-sets/members/remove")
    public ResponseEntity<Map<String, Object>> removeMember(@RequestParam Long userSetId,
                                                            @RequestParam Long userId,
                                                            HttpServletRequest httpRequest) {
        identifiedUserSetService.removeMember(userSetId, userId, httpRequest);
        return ResponseEntity.ok(Map.of(
                "status", true,
                "message", "Member removed successfully from Identified User Set"
        ));
    }

    @GetMapping("/identified-user-sets/job-status")
    public ResponseEntity<List<JobStatusDto>> getJobStatus(@RequestParam Long jobId,
                                                     HttpServletRequest httpRequest) {
        return ResponseEntity.ok(identifiedUserSetService.getUploadJobStatus(jobId, httpRequest));
    }

    // --- Legacy / Alternative Compatibility Endpoints (Exclusively POST & GET) ---

    @PostMapping("/createIdentifiedUserSet")
    public ResponseEntity<?> createIdentifiedUserSetCompat(@RequestParam String name,
                                                           @RequestParam(required = false) String description,
                                                           HttpServletRequest request) {
        CreateUserSetRequest dto = new CreateUserSetRequest();
        dto.setName(name);
        dto.setDescription(description);
        return ResponseEntity.ok(identifiedUserSetService.createUserSet(dto, request));
    }

    @GetMapping("/fetchIdentifiedUserSets")
    public ResponseEntity<?> fetchIdentifiedUserSetsCompat(HttpServletRequest request) {
        return ResponseEntity.ok(identifiedUserSetService.getUserSets(request));
    }

    @PostMapping("/changeIdentifiedUserSetStatus")
    public ResponseEntity<?> changeIdentifiedUserSetStatusCompat(@RequestParam Long id, HttpServletRequest request) {
        return ResponseEntity.ok(identifiedUserSetService.changeUserSetStatus(id, request));
    }

    @GetMapping("/identified-user-sets/user-ids")
    public ResponseEntity<List<Integer>> getUserSetIdsGet(@RequestParam Long userId,
                                                          @RequestParam(required = false) String tenantId,
                                                          HttpServletRequest httpRequest) {
        return ResponseEntity.ok(identifiedUserSetService.getUserSetIdsForUser(userId, tenantId, httpRequest));
    }
}

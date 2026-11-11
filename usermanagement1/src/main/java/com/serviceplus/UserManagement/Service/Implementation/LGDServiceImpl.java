package com.serviceplus.UserManagement.Service.Implementation;

import com.google.gson.reflect.TypeToken;
import com.serviceplus.UserManagement.Dto.*;
import com.serviceplus.UserManagement.Dto.LGDCommonDto.LocalBodyData;
import com.serviceplus.UserManagement.ExceptionHandler.SPRuntimeError;
import com.serviceplus.UserManagement.ApiExecutor.ApiExecutor;
import com.serviceplus.UserManagement.Service.LGDService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.serviceplus.UserManagement.util.Utility.*;

@Service
public class LGDServiceImpl implements LGDService{

	@Value("${lgd.service}")
	private String LGD_SERVICE;
	
	@Autowired
	private ApiExecutor<?> apiExecutor;
	
	@Override
	public String getClcName(Integer clcId) {
		
		try {
			String url = LGD_SERVICE.concat("unitName");

            Map<String, Object> body = new HashMap<>();
            body.put("unitCode", clcId);
			
			
			ResponseEntity<?> apiResponse = apiExecutor.callExternalEndpoint(
														String.class, HttpMethod.POST, new HashMap<>(),
														Collections.emptyMap(), url,
														entityToString(body), MediaType.APPLICATION_JSON);


            return (String) apiResponse.getBody();
				
		} catch (Exception e) {
			if(e instanceof SPRuntimeError)
				throw e;
			
			e.printStackTrace();
			throw new SPRuntimeError("Something went wrong [UM-LG 1]", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	public LGDCommonDto getOrgLevelName(Integer orgUnitCode) {
		
		try {
			String url = LGD_SERVICE.concat("/getOrgLevelDetails?orgLevelCode={orgLevelCode}");
			
			Map<String,Object> params = Map.of("orgLevelCode", orgUnitCode);
			
			ResponseEntity<?> apiResponse = apiExecutor.callExternalEndpoint(
														String.class, HttpMethod.GET, new HashMap<>(), 
														params, url,
														"", MediaType.APPLICATION_JSON);


            return (LGDCommonDto) stringToEntity ((String) apiResponse.getBody(),LGDCommonDto.class);
				
		} catch (Exception e) {
			if(e instanceof SPRuntimeError)
				throw e;
			
			e.printStackTrace();
			throw new SPRuntimeError("Something went wrong [UM-LG 2]", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public LGDCommonDto getLocationName(List<Integer> locId,boolean isState,Integer parentLocation,Integer subOrgCode) {
		
		try {
			String url = LGD_SERVICE.concat("/batch/getLocationDetails?orgUnitCode={orgUnitCode}&orgLocatedLevelCode={orgLocatedLevelCode}&"
															+ "isState={isState}&parentLocation={parentLocation}");
			
			Map<String,Object> params = Map.of("orgUnitCode", parentLocation,"orgLocatedLevelCode",subOrgCode,
												"isState",isState,"parentLocation",parentLocation);
			
			
			BatchApiRequestDto body = new BatchApiRequestDto(locId);
			ResponseEntity<?> apiResponse = apiExecutor.callExternalEndpoint(
														String.class, HttpMethod.POST, new HashMap<>(), 
														params, url,
														entityToString(body), MediaType.APPLICATION_JSON);

			Type listType = new TypeToken<List<LGDCommonDto.LineDepartmentData>>() {}.getType();
			List<LGDCommonDto.LineDepartmentData> result = (List<LGDCommonDto.LineDepartmentData>) stringToEntityUsingType((String) apiResponse.getBody(), listType);
			return new LGDCommonDto(null,result);
			
		} catch (Exception e) {
			if(e instanceof SPRuntimeError)
				throw e;
			
			e.printStackTrace();
			throw new SPRuntimeError("Something went wrong [UM-LG 3]", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public LGDCommonDto getLocationNameLB(Integer type,List<Integer> locId,Integer stateID) {
		
		try {
			String url = LGD_SERVICE.concat("/batch/findDistrictPanchyatList?type={type}&stateID={stateID}");
			
			Map<String,Object> params = Map.of("type", type,"stateID",stateID);
			BatchApiRequestDto body = new BatchApiRequestDto(locId);
			
			ResponseEntity<?> apiResponse = apiExecutor.callExternalEndpoint(
														String.class, HttpMethod.POST, new HashMap<>(), 
														params, url,
														entityToString(body), MediaType.APPLICATION_JSON);

				
			Type listType = new TypeToken<List<LGDCommonDto.LocalBodyData>>() {}.getType();
			List<LGDCommonDto.LocalBodyData> result = (List<LocalBodyData>) stringToEntityUsingType ((String) apiResponse.getBody(),listType);
			return new LGDCommonDto(result,null);
				
			
		} catch (Exception e) {
			if(e instanceof SPRuntimeError)
				throw e;
			
			e.printStackTrace();
			throw new SPRuntimeError("Something went wrong [UM-LG 4]", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<AllOfficeLocationDto> findAllOfficeLocation(Integer locationTypeId, Integer state, Integer subOffice) {
		try {
			String url = LGD_SERVICE.concat("/batch/findAllOfficeLocation?lType={lType}&stateID={stateID}&subOrgCode={subOrgCode}");
			
			Map<String,Object> params = Map.of("lType", locationTypeId,"stateID",state,"subOrgCode",(subOffice == null ? 0 : subOffice));
			
			ResponseEntity<?> apiResponse = apiExecutor.callExternalEndpoint(
														String.class, HttpMethod.GET, new HashMap<>(), 
														params, url,
														"", MediaType.APPLICATION_JSON);

				
			Type listType = new TypeToken<List<AllOfficeLocationDto>>() {}.getType();
            return (List<AllOfficeLocationDto>) stringToEntityUsingType ((String) apiResponse.getBody(),listType);
				
			
		} catch (Exception e) {
			if(e instanceof SPRuntimeError)
				throw e;
			
			e.printStackTrace();
			throw new SPRuntimeError("Something went wrong [UM-LG 5]", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@Override
	public LGDCommonDto getLocationName(Integer locId) {
		
		try {
			String url = LGD_SERVICE.concat("/getLocationDetails?orgUnitCode={orgUnitCode}");
			
			Map<String,Object> params = Map.of("orgUnitCode", locId);
			
			ResponseEntity<?> apiResponse = apiExecutor.callExternalEndpoint(
														String.class, HttpMethod.GET, new HashMap<>(), 
														params, url,
														"", MediaType.APPLICATION_JSON);

            return (LGDCommonDto) stringToEntity ((String) apiResponse.getBody(),LGDCommonDto.class);
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new SPRuntimeError("Something went wrong [UM-LG 6]", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

    @Override
    @SuppressWarnings("unchecked")
    public List<CreateUserDto.SpecificLocation> fetchLocations(
            Integer categoryId,
            Integer entityId,
            Integer levelId,
            Integer parentUnitId,
            Integer locationId,
            Integer clcId,
            Integer allOrSpecific,
            List<CreateUserDto.SpecificLocation> unitId,
            boolean tenantAdmin) {

        try {

            String url = LGD_SERVICE.concat("levelWiseUnits");

            LevelWiseUnitRequest body = new LevelWiseUnitRequest();

            body.setLevelCode(levelId);
            body.setEntityCode(entityId);
            body.setClc(clcId == null ? 0 : clcId);
            body.setParentUnitCode(parentUnitId == null ? 0 : parentUnitId);
            body.setLocationCode(locationId == null ? 0 : locationId);

            if (unitId != null && !unitId.isEmpty()) {

                List<LevelWiseUnitRequest.UnitId> units =
                        unitId.stream()
                                .map(unit -> {
                                    LevelWiseUnitRequest.UnitId dto =
                                            new LevelWiseUnitRequest.UnitId();
                                    dto.setUnitCode(unit.getEntityUnitCode());
                                    return dto;
                                })
                                .toList();

                body.setUnitId(units);
            }

            System.out.println(entityToString(body));

            ResponseEntity<?> apiResponse =
                    apiExecutor.callExternalEndpoint(
                            String.class,
                            HttpMethod.POST,
                            new HashMap<>(),
                            new HashMap<>(),
                            url,
                            entityToString(body),
                            MediaType.APPLICATION_JSON);

            if (apiResponse == null || apiResponse.getBody() == null) {
                return Collections.emptyList();
            }

            Type listType = new TypeToken<List<CreateUserDto.SpecificLocation>>() {
            }.getType();

            return (List<CreateUserDto.SpecificLocation>) stringToEntityUsingType((String) apiResponse.getBody(), listType);

        } catch (Exception ex) {

            if (ex instanceof SPRuntimeError) {
                throw ex;
            }

            throw new SPRuntimeError("Unable to fetch locations", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public EntityLevelDetailsDto getEntityLevelDetails(Integer entityCode, Integer levelCode) {

        try {

            String url = LGD_SERVICE.concat("entity-level-details");

            Map<String, Object> body = new HashMap<>();
            body.put("levelCode", levelCode);
            body.put("entityCode", entityCode);

            ResponseEntity<?> apiResponse =
                    apiExecutor.callExternalEndpoint(
                            String.class,
                            HttpMethod.POST,
                            new HashMap<>(),
                            Collections.emptyMap(),
                            url,
                            entityToString(body),
                            MediaType.APPLICATION_JSON);

            return (EntityLevelDetailsDto) stringToEntity((String) apiResponse.getBody(), EntityLevelDetailsDto.class);

        } catch (Exception ex) {

            if (ex instanceof SPRuntimeError) {
                throw ex;
            }

            ex.printStackTrace();
            throw new SPRuntimeError("Something went wrong [UM-LG 8]", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public String getDesignationName(Integer designationCode) {

        try {

            String url = LGD_SERVICE.concat("designationName");

            Map<String, Object> body = new HashMap<>();
            body.put("designationCode", designationCode);

            ResponseEntity<?> apiResponse =
                    apiExecutor.callExternalEndpoint(
                            String.class,
                            HttpMethod.POST,
                            new HashMap<>(),
                            Collections.emptyMap(),
                            url,
                            entityToString(body),
                            MediaType.APPLICATION_JSON);

            DesignationDTO designation = (DesignationDTO) stringToEntity((String) apiResponse.getBody(), DesignationDTO.class);
            return designation.getDesignationName();

        } catch (Exception e) {

            if (e instanceof SPRuntimeError) {
                throw e;
            }

            e.printStackTrace();
            throw new SPRuntimeError("Something went wrong [UM-LG 9]", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

	@SuppressWarnings("unchecked")
	@Override
	public List<LocationDetailsDTO> findChildLocationDetails(Integer entityId, Integer locationId,
			Integer entityLevelId) {

        try {

            String url = LGD_SERVICE.concat("get-child-entity-units");

            Map<String, Object> body = new HashMap<>();
            body.put("parentEntityId", entityId);
            body.put("parentEntityLevelId", entityLevelId);
            body.put("parentEntityUnitId", locationId);

            ResponseEntity<?> apiResponse =
                    apiExecutor.callExternalEndpoint(
                            String.class,
                            HttpMethod.POST,
                            new HashMap<>(),
                            Collections.emptyMap(),
                            url,
                            entityToString(body),
                            MediaType.APPLICATION_JSON);

            if (apiResponse == null || apiResponse.getBody() == null) {
                return Collections.emptyList();
            }

            Type listType = new TypeToken<List<LocationDetailsDTO>>() {
            }.getType();

            return (List<LocationDetailsDTO>) stringToEntityUsingType(
                    (String) apiResponse.getBody(),
                    listType
            );

        } catch (Exception ex) {

            if (ex instanceof SPRuntimeError) {
                throw ex;
            }

            ex.printStackTrace();
            throw new SPRuntimeError("Something went wrong [UM-LG 8]", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}

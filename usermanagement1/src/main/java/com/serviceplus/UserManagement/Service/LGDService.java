package com.serviceplus.UserManagement.Service;

import java.util.List;

import com.serviceplus.UserManagement.Dto.AllOfficeLocationDto;
import com.serviceplus.UserManagement.Dto.CreateUserDto;
import com.serviceplus.UserManagement.Dto.EntityLevelDetailsDto;
import com.serviceplus.UserManagement.Dto.LGDCommonDto;
import com.serviceplus.UserManagement.Dto.LocationDetailsDTO;

public interface LGDService {

	String getClcName(Integer clcId);
	
	LGDCommonDto getOrgLevelName(Integer subOrgCode);
	
	LGDCommonDto getLocationName(List<Integer> locId,boolean isState,Integer parentLocation,Integer orgLocatedLevelCode);
	
	LGDCommonDto getLocationNameLB(Integer type,List<Integer> batchIds, Integer integer);

	List<AllOfficeLocationDto> findAllOfficeLocation(Integer locationTypeId, Integer state, Integer subOffice);
	
	LGDCommonDto getLocationName(Integer locId);

    List<CreateUserDto.SpecificLocation> fetchLocations(Integer categoryId, Integer entityId, Integer levelId, Integer parentUnitId, Integer locationId, Integer clcId, Integer allOrSpecific, List<CreateUserDto.SpecificLocation> unitId, boolean tenantAdmin);

    EntityLevelDetailsDto getEntityLevelDetails(Integer entityCode, Integer levelCode);

    String getDesignationName(Integer designationCode);

	List<LocationDetailsDTO> findChildLocationDetails(Integer sourceEntityId, Integer sourceLocationId,
			Integer sourceEntityLevelId);
}

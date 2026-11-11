package com.serviceplus.UserManagement.Dto;

import java.util.List;

public class LevelWiseUnitRequest {

    private Integer levelCode;

    private Integer entityCode;

    private Integer clc;

    private Integer parentUnitCode;

    private Integer locationCode;

    private List<UnitId> unitId;

    public static class UnitId {

        private Integer unitCode;

        public Integer getUnitCode() {
            return unitCode;
        }

        public void setUnitCode(Integer unitCode) {
            this.unitCode = unitCode;
        }
    }

    public Integer getLevelCode() {
        return levelCode;
    }

    public void setLevelCode(Integer levelCode) {
        this.levelCode = levelCode;
    }

    public Integer getEntityCode() {
        return entityCode;
    }

    public void setEntityCode(Integer entityCode) {
        this.entityCode = entityCode;
    }

    public Integer getClc() {
        return clc;
    }

    public void setClc(Integer clc) {
        this.clc = clc;
    }

    public Integer getParentUnitCode() {
        return parentUnitCode;
    }

    public void setParentUnitCode(Integer parentUnitCode) {
        this.parentUnitCode = parentUnitCode;
    }

    public Integer getLocationCode() {
        return locationCode;
    }

    public void setLocationCode(Integer locationCode) {
        this.locationCode = locationCode;
    }

    public List<UnitId> getUnitId() {
        return unitId;
    }

    public void setUnitId(List<UnitId> unitId) {
        this.unitId = unitId;
    }
}
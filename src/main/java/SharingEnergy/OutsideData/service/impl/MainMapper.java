package SharingEnergy.OutsideData.service.impl;

import SharingEnergy.Object.OutsideDataObject;
import SharingEnergy.Object.ResponseObject;

import java.util.ArrayList;
import java.util.Map;

/**
 * @ Class Name   : MainMapper.java
 * @ Modification : SharingEnergy MAIN MAPPER
 * @
 * @ 최초 생성일  최초 생성자
 * @ ---------    ---------
 * @ 2020.11.24.    임태준
 * @
 * @  수정일        수정자
 * @ ---------    ---------
 * @
 **/


public interface MainMapper {

    int outsideDataInsert(ArrayList<OutsideDataObject> outsideDataList) throws Exception;

    int outsideQuantumDataInsert(ArrayList<OutsideDataObject> outsideDataList) throws Exception;

    int errorLogInsert(ResponseObject responseObject) throws Exception;

    Map<String, Object> selectLatestData(OutsideDataObject outsideDataObject) throws Exception;

    int deleteHourData() throws Exception;

    int insertHourData() throws Exception;

    int deleteDayData() throws Exception;

    int insertDayData() throws Exception;

    int deleteMonthData() throws Exception;

    int insertMonthData() throws Exception;

    int deleteYearData() throws Exception;

    int insertYearData() throws Exception;
}

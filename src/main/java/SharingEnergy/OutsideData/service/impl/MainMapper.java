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

    int errorLogInsert(ResponseObject responseObject) throws Exception;

    Map<String, Object> selectLatestData(OutsideDataObject outsideDataObject) throws Exception;
}

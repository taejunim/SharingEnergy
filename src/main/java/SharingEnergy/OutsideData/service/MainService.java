package SharingEnergy.OutsideData.service;

import SharingEnergy.Object.OutsideDataObject;
import SharingEnergy.Object.ResponseObject;

import java.util.ArrayList;


/**
 * @ Class Name   : MainService.java
 * @ Modification : SharingEnergy MAIN SERVICE
 * @
 * @ 최초 생성일  최초 생성자
 * @ ---------    ---------
 * @ 2020.11.24.    임태준
 * @
 * @  수정일        수정자
 * @ ---------    ---------
 * @
 **/
public interface MainService {

    int outsideDataInsert(ArrayList<OutsideDataObject> outsideDataList) throws Exception;

    int errorLogInsert(ResponseObject responseObject) throws Exception;

}

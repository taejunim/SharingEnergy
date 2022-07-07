package SharingEnergy;

import SharingEnergy.Object.KepcoAmi;
import SharingEnergy.Object.WeatherInterface;

import java.util.HashMap;
import java.util.List;


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



    List<KepcoAmi> getRegisterAmi(HashMap<String,Object> amiList) throws Exception;

    void insertAmiData(List<KepcoAmi> amiDataList) throws Exception;

    void insertWeatherData(WeatherInterface weatherInterface) throws Exception;
}

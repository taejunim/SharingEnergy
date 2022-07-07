package SharingEnergy;

import SharingEnergy.Object.KepcoAmi;
import SharingEnergy.Object.WeatherInterface;

import java.util.HashMap;
import java.util.List;

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



    int deleteHourData() throws Exception;

    int insertHourData() throws Exception;

    int deleteDayData() throws Exception;

    int insertDayData() throws Exception;

    int deleteMonthData() throws Exception;

    int insertMonthData() throws Exception;

    int deleteYearData() throws Exception;

    int insertYearData() throws Exception;




    List<KepcoAmi> getRegisterAmi(HashMap<String,Object> amiList) throws Exception;

    /**
     * 15분 한전 AMI 데이터 등록
     *
     * @return
     */
    void insertAmi15MinuteData(List<KepcoAmi> amiDataList) throws Exception;

    /**
     * 한시간 한전 AMI 데이터 등록
     *
     * @return
     */
    void insertAmiHourData(List<KepcoAmi> amiDataList) throws Exception;

    /**
     * 하루 한전 AMI 데이터 등록
     *
     * @return
     */
    void insertAmiDayData(List<KepcoAmi> amiDataList) throws Exception;

    /**
     * 날씨 정보 등록
     *
     * @return
     */
    void insertWeatherData(WeatherInterface weatherInterface) throws Exception;
}

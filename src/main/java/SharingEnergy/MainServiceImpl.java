package SharingEnergy;

import SharingEnergy.Object.KepcoAmi;
import SharingEnergy.Object.WeatherInterface;
import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;



/**
 * @ Class Name   : MainServiceImpl.java
 * @ Modification : SharingEnergy MAIN SERVICE IMPL
 * @
 * @ 최초 생성일  최초 생성자
 * @ ---------    ---------
 * @ 2020.11.24.    임태준
 * @
 * @  수정일        수정자
 * @ ---------    ---------
 * @
 **/
@Service
public class MainServiceImpl implements MainService {

    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private SqlSession sqlSession;


    public List<KepcoAmi> getRegisterAmi(HashMap<String,Object> amiList) throws Exception {

        MainMapper mainMapper = sqlSession.getMapper(MainMapper.class);

        return mainMapper.getRegisterAmi(amiList);
    }

    /**
     * 한전 AMI 데이터 등록
     *
     * @return
     */
    public void insertAmiData(List<KepcoAmi> amiDataList) throws Exception {

        MainMapper mainMapper = sqlSession.getMapper(MainMapper.class);

        try {
            mainMapper.insertAmi15MinuteData(amiDataList);
        } catch (DataIntegrityViolationException e) {
            System.out.println("기존에 등록된 한전 15분 단위 AMI 데이터 입니다.");
        }
        mainMapper.insertAmiHourData(amiDataList);
        mainMapper.insertAmiDayData(amiDataList);
    }

    /**
     * 날씨정보 등록
     */
    public void insertWeatherData(WeatherInterface weatherInterface) throws Exception {

        MainMapper mainMapper = sqlSession.getMapper(MainMapper.class);

        mainMapper.insertWeatherData(weatherInterface);
    }
}

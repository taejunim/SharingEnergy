package SharingEnergy;

import SharingEnergy.Object.RequestQuantumDataObject;
import SharingEnergy.Object.ResponseObject;
import SharingEnergy.Object.ResponseQuantumDataObject;
import SharingEnergy.OutsideData.service.MainService;
import SharingEnergy.OutsideData.service.impl.MainMapper;
import com.google.gson.Gson;
import org.apache.ibatis.session.SqlSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.client.RestTemplate;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class Scheduler {

    Logger logger = LogManager.getLogger(Scheduler.class);

    @Autowired
    private SqlSession sqlSession ;

    @Autowired
    private MainService mainService;

    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    String[] deviceArray = {"001", "004", "004", "005"};

    /*매월 1일 0시 30분 명예의전당 입력*/
    @Scheduled(cron="0 0/5 * * * *")
    public void HofScheduler() {

        ResponseQuantumDataObject responseQuantumDataObject = new ResponseQuantumDataObject();
        ResponseObject responseObject;

        try{

            // RestTemplate 에 MessageConverter 세팅
            List<HttpMessageConverter<?>> converters = new ArrayList<HttpMessageConverter<?>>();
            converters.add(new FormHttpMessageConverter());
            converters.add(new StringHttpMessageConverter());
            converters.add(new GsonHttpMessageConverter());

            MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();

            // Note: here we are making this converter to process any kind of response,
            // not only application/*json, which is the default behaviour
            converter.setSupportedMediaTypes(Collections.singletonList(MediaType.ALL));
            converters.add(converter);

            //Timeout
            HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
            factory.setConnectTimeout(10*1000);
            factory.setReadTimeout(10*1000);

            RestTemplate restTemplate = new RestTemplate(factory);
            restTemplate.setMessageConverters(converters);

            ArrayList<RequestQuantumDataObject> requestQuantumDataObjectArrayList = new ArrayList<RequestQuantumDataObject>();

            long endDate = System.currentTimeMillis();
            String endDttm = simpleDateFormat.format(endDate);

            for (int i=0; i<deviceArray.length; i++) {

                RequestQuantumDataObject requestQuantumDataObject = new RequestQuantumDataObject();
                requestQuantumDataObject.setEndDttm(endDttm);

                //PV
                if (deviceArray[i].equals("001")) {
                    requestQuantumDataObject.setDeviceGbnCd(deviceArray[i]);
                }

                //ESS
                else if (deviceArray[i].equals("004")) {

                    requestQuantumDataObject.setDeviceGbnCd(deviceArray[i]);

                    if (i == 1) {
                        requestQuantumDataObject.setChgrGbnCd("001");
                    } else if (i ==2) {
                        requestQuantumDataObject.setChgrGbnCd("002");
                    }
                }

                //EV
                else if (deviceArray[i].equals("005")) {
                    requestQuantumDataObject.setDeviceGbnCd(deviceArray[i]);
                }

                requestQuantumDataObjectArrayList.add(requestQuantumDataObject);
            }

            logger.info("requestQuantumDataObjectArrayList : " + requestQuantumDataObjectArrayList);

            MainMapper maintenanceMapper = sqlSession.getMapper(MainMapper.class);

            // REST API 호출
            responseQuantumDataObject = restTemplate.postForObject("http://112.133.107.227:30000/SharingEnergy/responseData.do", requestQuantumDataObjectArrayList, ResponseQuantumDataObject.class);

            logger.info("[ 퀀텀 데이터 수신 ]");
            logger.info(responseQuantumDataObject);

            //퀀텀 데이터
            int insertResult = 0;

            if (responseQuantumDataObject.outsideDataList.size() > 0) {
                insertResult = maintenanceMapper.outsideQuantumDataInsert(responseQuantumDataObject.outsideDataList);
                logger.info("[ 퀀텀 데이터 등록 성공 - " + insertResult + " 건]");
            } else {

                responseObject = makeResponseObject("E002", "fail", "List Size 오류");

                responseObject.dataList = new Gson().toJson(responseQuantumDataObject.outsideDataList);
                mainService.errorLogInsert(responseObject);
                logger.info("[ 퀀텀 데이터 등록 실패 ]");
            }

            insertSummaryData();

        }catch (DuplicateKeyException duplicateKeyException) {
            duplicateKeyException.printStackTrace();
            logger.info("[ 퀀텀 데이터 PRIMARY KEY 중복 ]");

            responseObject = makeResponseObject("E001", "fail", "요청 파라미터 오류 : PRIMARY KEY 중복");
            responseObject.dataList = new Gson().toJson(responseQuantumDataObject.outsideDataList);

            try {
                mainService.errorLogInsert(responseObject);
            } catch (Exception exceptionOfDuplicateKeyException) {
                exceptionOfDuplicateKeyException.printStackTrace();
                logger.info("[ 퀀텀 데이터 DuplicateKeyException - Exception ]");
            }

        } catch (Exception e) {
            e.printStackTrace();
            logger.info("[ 퀀텀 데이터 알 수 없는 오류 ]");

            responseObject = makeResponseObject("E999", "fail", "기타 오류");

            responseObject.dataList = new Gson().toJson(responseQuantumDataObject.outsideDataList);

            try {
                mainService.errorLogInsert(responseObject);
            } catch (Exception exceptionOfException) {
                exceptionOfException.printStackTrace();
                logger.info("[ 퀀텀 데이터 Exception - Exception ]");
            }
        }
    }

    //Summary Data Insert
    private void insertSummaryData() {

        MainMapper maintenanceMapper = sqlSession.getMapper(MainMapper.class);

        try {

            //시간별 데이터
            int deleteHourResult = maintenanceMapper.deleteHourData();
            int insertHourResult = maintenanceMapper.insertHourData();

            logger.info("deleteHourResult : " + deleteHourResult);
            logger.info("insertHourResult : " + insertHourResult);

            //일별 데이터
            int deleteDayResult = maintenanceMapper.deleteDayData();
            int insertDayResult = maintenanceMapper.insertDayData();

            logger.info("deleteDayResult : " + deleteDayResult);
            logger.info("insertDayResult : " + insertDayResult);

            //월별 데이터
            int deleteMonthResult = maintenanceMapper.deleteMonthData();
            int insertMonthResult = maintenanceMapper.insertMonthData();

            logger.info("deleteMonthResult : " + deleteMonthResult);
            logger.info("insertMonthResult : " + insertMonthResult);

            //년별 데이터
            int deleteYearResult = maintenanceMapper.deleteYearData();
            int insertYearResult = maintenanceMapper.insertYearData();

            logger.info("deleteYearResult : " + deleteYearResult);
            logger.info("insertYearResult : " + insertYearResult);
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("[ Summary Data Insert Exception Cause : " + e.getCause());
            logger.info("[ Summary Data Insert Exception Message : " + e.getMessage());
            logger.info("[ Summary Data Insert Exception StackTrace : " + e.getStackTrace());

            ResponseObject responseObject = makeResponseObject("E999", "fail", "Summary Data 작업 오류");

            responseObject.dataList = "";

            try {
                mainService.errorLogInsert(responseObject);
            } catch (Exception exceptionOfException) {
                exceptionOfException.printStackTrace();
                logger.info("[ Summary Data Insert Exception Exception - Exception ]");
            }
        }
    }

    private ResponseObject makeResponseObject(String code, String result, String message) {

        ResponseObject responseObject = new ResponseObject();

        responseObject.code = code;
        responseObject.result = result;
        responseObject.message = message;

        return responseObject;

    }
}

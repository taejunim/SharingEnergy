package SharingEnergy;

import SharingEnergy.Object.OutsideDataObject;
import SharingEnergy.Object.ResponseObject;
import SharingEnergy.OutsideData.service.MainService;
import SharingEnergy.OutsideData.service.impl.MainMapper;
import com.google.gson.Gson;
import org.apache.ibatis.session.SqlSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Controller
public class Main {

    Logger logger = LogManager.getLogger(Main.class);

    @Autowired
    private MainService mainService;

    @Autowired
    private SqlSession sqlSession ;

    @RequestMapping(value = "SharingEnergy/test.do")
    public String test() {

        logger.info("test");
        
        return "index";
    }

    @PostMapping(value = "SharingEnergy/postData.do")
    public @ResponseBody ResponseObject postData(@RequestBody ArrayList<OutsideDataObject> outsideDataList) {

        ResponseObject responseObject;

        logger.info("[ 데이터 수신 ]");

        try {

            for (int i=0; i<outsideDataList.size(); i++) {

                if (outsideDataList.get(i).getDeviceId() == null || outsideDataList.get(i).getDeviceId().equals("")) {
                    responseObject = makeResponseObject("E002", "fail", "요청 파라미터 오류 : deviceId 값 오류");
                    responseObject.outsideDataList = outsideDataList;
                    responseObject.dataList = new Gson().toJson(outsideDataList);

                    mainService.errorLogInsert(responseObject);

                    return responseObject;
                }

                if (outsideDataList.get(i).getDeviceGbnCd() == null || outsideDataList.get(i).getDeviceGbnCd().equals("")) {
                    responseObject = makeResponseObject("E002", "fail", "요청 파라미터 오류 : deviceGbnCd 값 오류");
                    responseObject.outsideDataList = outsideDataList;
                    responseObject.dataList = new Gson().toJson(outsideDataList);
                    mainService.errorLogInsert(responseObject);

                    return responseObject;
                }

                if (outsideDataList.get(i).getOrgGbnCd() == null || outsideDataList.get(i).getOrgGbnCd().equals("")) {
                    responseObject = makeResponseObject("E002", "fail", "요청 파라미터 오류 : orgGbnCd 값 오류");
                    responseObject.outsideDataList = outsideDataList;
                    responseObject.dataList = new Gson().toJson(outsideDataList);
                    mainService.errorLogInsert(responseObject);

                    return responseObject;
                }

                if (outsideDataList.get(i).getOrgCd() == null || outsideDataList.get(i).getOrgCd().equals("")) {
                    responseObject = makeResponseObject("E002", "fail", "요청 파라미터 오류 : orgCd 값 오류");
                    responseObject.outsideDataList = outsideDataList;
                    responseObject.dataList = new Gson().toJson(outsideDataList);
                    mainService.errorLogInsert(responseObject);

                    return responseObject;
                }

                if (outsideDataList.get(i).getChgrGbnCd() == null || outsideDataList.get(i).getChgrGbnCd().equals("")) {
                    responseObject = makeResponseObject("E002", "fail", "요청 파라미터 오류 : chgrGbnCd 값 오류");
                    responseObject.outsideDataList = outsideDataList;
                    responseObject.dataList = new Gson().toJson(outsideDataList);
                    mainService.errorLogInsert(responseObject);

                    return responseObject;
                }

                if (outsideDataList.get(i).getMeasureDttm() == null || outsideDataList.get(i).getMeasureDttm().equals("")) {
                    responseObject = makeResponseObject("E002", "fail", "요청 파라미터 오류 : measureDttm 값 오류");
                    responseObject.outsideDataList = outsideDataList;
                    responseObject.dataList = new Gson().toJson(outsideDataList);
                    mainService.errorLogInsert(responseObject);

                    return responseObject;
                }

                logger.info("[ ----------------------- " + (i+1) + " ----------------------- ]");
                logger.info("[ deviceId : " + outsideDataList.get(i).getDeviceId() + " ]");
                logger.info("[ deviceGbnCd : " + outsideDataList.get(i).getDeviceGbnCd() + " ]");
                logger.info("[ chgrGbnCd : " + outsideDataList.get(i).getChgrGbnCd() + " ]");
                logger.info("[ measureDttm : " + outsideDataList.get(i).getMeasureDttm() + " ]");
                logger.info("[ actElecPwr : " + outsideDataList.get(i).getActElecPwr() + " ]");
                logger.info("[ actElecEnergy : " + outsideDataList.get(i).getActElecEnergy() + " ]");
                logger.info("[ orgGbnCd : " + outsideDataList.get(i).getOrgGbnCd() + " ]");
                logger.info("[ orgCd : " + outsideDataList.get(i).getOrgCd() + " ]");
                logger.info("[ ------------------------------------------------- ]");
            }

            mainService.outsideDataInsert(outsideDataList);
            logger.info("[ 데이터 등록 성공 ]");

            responseObject = makeResponseObject("0000", "success", "데이터 등록 성공");

        } catch (DuplicateKeyException duplicateKeyException) {
            duplicateKeyException.printStackTrace();
            logger.info("[ PRIMARY KEY 중복 ]");

            responseObject = makeResponseObject("E001", "fail", "요청 파라미터 오류 : PRIMARY KEY 중복");
            responseObject.outsideDataList = outsideDataList;
            responseObject.dataList = new Gson().toJson(outsideDataList);

            try {
                mainService.errorLogInsert(responseObject);
            } catch (Exception exceptionOfDuplicateKeyException) {
                exceptionOfDuplicateKeyException.printStackTrace();
                logger.info("[ DuplicateKeyException - Exception ]");
            }

        } catch (Exception e) {
            e.printStackTrace();
            logger.info("[ 알 수 없는 오류 ]");

            responseObject = makeResponseObject("E999", "fail", "기타 오류");
            responseObject.outsideDataList = outsideDataList;
            responseObject.dataList = new Gson().toJson(outsideDataList);

            try {
                mainService.errorLogInsert(responseObject);
            } catch (Exception exceptionOfException) {
                exceptionOfException.printStackTrace();
                logger.info("[ Exception - Exception ]");
            }
        }

        responseObject.outsideDataList = outsideDataList;

        return responseObject;
    }

    private ResponseObject makeResponseObject(String code, String result, String message) {

        ResponseObject responseObject = new ResponseObject();

        responseObject.code = code;
        responseObject.result = result;
        responseObject.message = message;

        return responseObject;

    }

    @PostMapping(value = "SharingEnergy/checkLatestData.do")
    public @ResponseBody Map<String,Object> checkLatestData(@RequestBody OutsideDataObject outsideDataObject) {

        logger.info("[ 데이터 수신 ]");

        Map<String,Object> resultMap = new HashMap<String, Object>();

        try {

            if (outsideDataObject.getDeviceGbnCd() == null || outsideDataObject.getDeviceGbnCd().equals("")) {
                resultMap = makeResponseMap("E002", "fail", "요청 파라미터 오류 : deviceGbnCd 값 오류");
                resultMap.put("outsideData", outsideDataObject);

                return resultMap;
            }

            if (outsideDataObject.getOrgGbnCd() == null || outsideDataObject.getOrgGbnCd().equals("")) {
                resultMap = makeResponseMap("E002", "fail", "요청 파라미터 오류 : orgGbnCd 값 오류");
                resultMap.put("outsideData", outsideDataObject);

                return resultMap;
            }

            if (outsideDataObject.getOrgCd() == null || outsideDataObject.getOrgCd().equals("")) {
                resultMap = makeResponseMap("E002", "fail", "요청 파라미터 오류 : orgCd 값 오류");
                resultMap.put("outsideData", outsideDataObject);

                return resultMap;
            }

            logger.info("[ ------------------------------------------------- ]");
            logger.info("[ deviceId : " + outsideDataObject.getDeviceId() + " ]");
            logger.info("[ deviceGbnCd : " + outsideDataObject.getDeviceGbnCd() + " ]");
            logger.info("[ orgGbnCd : " + outsideDataObject.getOrgGbnCd() + " ]");
            logger.info("[ orgCd : " + outsideDataObject.getOrgCd() + " ]");
            logger.info("[ ------------------------------------------------- ]");

            MainMapper maintenanceMapper = sqlSession.getMapper(MainMapper.class);

            Map<String,Object> queryMap = maintenanceMapper.selectLatestData(outsideDataObject);

            resultMap.put("measureDttm", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(queryMap.get("MEASURE_DTTM")));

            logger.info("[ 최근 데이터 등록 일시 : " + resultMap.get("measureDttm") + " ]");

        } catch (Exception e) {
            e.printStackTrace();
            logger.info("[ 알 수 없는 오류 ]");

            resultMap = makeResponseMap("E999", "fail", "기타 오류");
            resultMap.put("outsideData", outsideDataObject);

            return resultMap;
        }

        return resultMap;
    }

    private Map<String,Object> makeResponseMap(String code, String result, String message) {

        Map<String,Object> resultMap = new HashMap<String, Object>();

        resultMap.put("code", code);
        resultMap.put("result", result);
        resultMap.put("message", message);

        return resultMap;

    }
}

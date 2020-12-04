package SharingEnergy;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;


public class Scheduler {

    Logger logger = LogManager.getLogger(Scheduler.class);

    String[] deviceArray = {"001", "004", "005"};

    /*매월 1일 0시 30분 명예의전당 입력*/
    @Scheduled(cron="*/10 * * * * *")
    public void HofScheduler() {
//        try{
//
//            // RestTemplate 에 MessageConverter 세팅
//            List<HttpMessageConverter<?>> converters = new ArrayList<HttpMessageConverter<?>>();
//            converters.add(new FormHttpMessageConverter());
//            converters.add(new StringHttpMessageConverter());
//            converters.add(new MappingJackson2HttpMessageConverter());
//            converters.add(new GsonHttpMessageConverter());
//
//            RestTemplate restTemplate = new RestTemplate();
//            restTemplate.setMessageConverters(converters);
//
////            // parameter 세팅
////            MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
////            map.add("str", "thisistest");
//
//            ArrayList<RequestQuantumDataObject> requestQuantumDataObjectArrayList = new ArrayList<RequestQuantumDataObject>();
//
//            for (int i=0; i<deviceArray.length; i++) {
//
//                System.out.println(deviceArray[i]);
//                RequestQuantumDataObject requestQuantumDataObject = new RequestQuantumDataObject();
//
//                requestQuantumDataObject.setStartDttm("2020-12-04 14:40");
//
//                //PV
//                if (deviceArray[i].equals("001")) {
//                    requestQuantumDataObject.setDeviceGbnCd(deviceArray[0]);
//                    requestQuantumDataObject.setChgrGbnCd("001");
//
//                    requestQuantumDataObjectArrayList.add(requestQuantumDataObject);
//                }
//
//                //ESS
//                else if (deviceArray[i].equals("004")) {
//                    requestQuantumDataObject.setDeviceGbnCd(deviceArray[1]);
//                    requestQuantumDataObject.setChgrGbnCd("001");
//
//                    requestQuantumDataObjectArrayList.add(requestQuantumDataObject);
//
//                    requestQuantumDataObject.setChgrGbnCd("002");
//
//                    requestQuantumDataObjectArrayList.add(requestQuantumDataObject);
//                }
//
//                //EV
//                else if (deviceArray[i].equals("005")) {
//                    requestQuantumDataObject.setDeviceGbnCd(deviceArray[2]);
//                    requestQuantumDataObject.setChgrGbnCd("001");
//                    requestQuantumDataObjectArrayList.add(requestQuantumDataObject);
//
//                }
//
//
//            }
//
//            // REST API 호출
//            ResponseObject responseObject = restTemplate.postForObject("http://172.30.1.51:8080/SharingEnergy/checkLatestData.do", requestQuantumDataObjectArrayList, ResponseObject.class);
//            System.out.println("------------------ TEST 결과 ------------------");
//            System.out.println(responseObject);
//
//
//
//        }catch(Exception e){
//            e.printStackTrace();
//        }
    }
}

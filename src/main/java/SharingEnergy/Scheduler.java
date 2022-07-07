package SharingEnergy;

import SharingEnergy.Object.KepcoAmi;
import SharingEnergy.Object.WeatherInterface;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;



public class Scheduler {

    @Autowired
    private MainService mainService;

    //EDSM 서비스키
    private String serviceKey = "8a368177bv5os995vpmm";
    //EDSM 응답 형식(json)

    private String returnType = "02";
    //고객번호 API URL

    private String customerUrl = "https://opm.kepco.co.kr:11080/OpenAPI/getCustNoList.do";
    //계기번호 API URL

    private String meterUrl = "https://opm.kepco.co.kr:11080/OpenAPI/getCustMeterList.do";
    //5분 단위 전력소비 데이터(일반) URL

    private String minuteUseUrl = "https://opm.kepco.co.kr:11080/OpenAPI/getMinuteLpData.do";

    public Gson gson = new Gson();

    /**
     * getKepcoAmiData()
     * 한전 AMI 고객 List API 호출 하여 DB 고객정보와 비교
     * 유효한 고객 정보로 15분 단위 호출 후 저장
     **/
    @Scheduled(cron = "0 1/15 * * * *")
    public void getKepcoAmiData() {

        System.out.println("[ ------------------------------------------------- ]");
        System.out.println("[ " + new SimpleDateFormat("yyyy년 MM월 dd일 HH:mm:ss").format(new Date()) + " - 한전 데이터 Scheduler 실행 ====> ]");
        System.out.println("[ ------------------------------------------------- ]");

        RestTemplate restTemplate = new RestTemplate();
        final HttpEntity entity = new HttpEntity(new HttpHeaders());
        try {
            /* 고객정보 URL 세팅 */
            String fullMeterUrl = meterUrl + "?serviceKey=" + serviceKey + "&returnType=" + returnType;
            URI meterURI = new URI(fullMeterUrl);
            System.out.println("fullMeterUrl : " + fullMeterUrl);
            ResponseEntity<String> meterApiResponse = restTemplate.exchange(meterURI, HttpMethod.GET, entity, String.class);

            if (meterApiResponse.getStatusCode().equals(HttpStatus.OK)) {
                Type listType = new TypeToken<ArrayList<KepcoAmi>>() {
                }.getType();

                JsonObject meterApiResponseApiBody = gson.fromJson(meterApiResponse.getBody(), JsonObject.class);
                List<KepcoAmi> amiList = gson.fromJson(meterApiResponseApiBody.get("custMeterInfoList").toString(), listType);

                //한전 API에서 가져온 AMI 목록 체크
                if (amiList.size() > 0 && amiList.get(0).getCustNo() != null) {

                    HashMap<String,Object> parameter = new HashMap<>();
                    parameter.put("list", amiList);
                    //한전 API에서 가져온 AMI와 CEMS에 등록된 AMI 목록 비교
                    amiList = mainService.getRegisterAmi(parameter);

                    //한전 API에서 가져온 미터기 정보와 CEMS에 등록된 미터기 정보 중 매칭되는 것이 있으면, 저압/고압 구분을 위한 API 호출
                    if(amiList.size() > 0 ){
                        String fullCustomerUrl = customerUrl + "?serviceKey=" + serviceKey + "&returnType=" + returnType;
                        URI customerURI = new URI(fullCustomerUrl);
                        System.out.println("fullCustomerUrl : " + fullCustomerUrl);
                        ResponseEntity<String> customerApiResponse = restTemplate.exchange(customerURI, HttpMethod.GET, entity, String.class);

                        if (customerApiResponse.getStatusCode().equals(HttpStatus.OK)) {
                            JsonObject customerApiResponseBody = gson.fromJson(customerApiResponse.getBody(), JsonObject.class);
                            List<KepcoAmi> customerList = gson.fromJson(customerApiResponseBody.get("custNoInfoList").toString(), listType);

                            // 포맷 정의
                            SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmm");

                            //고압용 조회일시 (실시간)
                            Date highPressureDate = new Date();
                            String highPressureFormatDate = format.format(highPressureDate);

                            //저압용 조회일시 (이틀 전)
                            Calendar calendar =  Calendar.getInstance();
                            calendar.add(Calendar.DATE, -2);
                            Date lowPressureDate = new Date(calendar.getTimeInMillis());
                            String lowPressureFormatDate = format.format(lowPressureDate);

                            //저압이든 고압이든 일자만 다르고 시간은 같음
                            String meteringTime   = highPressureFormatDate.substring(8,10) + getMinute(highPressureFormatDate.substring(10,12));

                            for (KepcoAmi ami : amiList) {//url 정의 (서비스 키, 데이터 타입, 고객번호 SET
                                String useUrl = minuteUseUrl + "?serviceKey=" + serviceKey + "&returnType=" + returnType + "&custNo=" + ami.getCustNo();
                                //저압,고압 여부
                                String lvHvVal = customerList.stream().filter(o -> o.getCustNo().equals(ami.getCustNo())).findFirst().get().getLvHvVal();
                                ami.setLvHvVal(lvHvVal);
                                ami.setMeteringTime(meteringTime);

                                if (lvHvVal.equals("저압")) {
                                    String formatDate = lowPressureFormatDate.substring(0, 8);
                                    ami.setMeteringDate(formatDate);

                                    //URL 검색일시 set
                                    useUrl += "&dateTime=" + formatDate + meteringTime;

                                } else if (lvHvVal.equals("고압")) {
                                    String formatDate = highPressureFormatDate.substring(0, 8);
                                    ami.setMeteringDate(formatDate);

                                    //URL 검색일시 set
                                    useUrl += "&dateTime=" + formatDate + meteringTime;
                                }

                                if ((lvHvVal.equals("저압") && meteringTime.startsWith("00", 2)) || lvHvVal.equals("고압")) {

                                    URI useURI = new URI(useUrl);
                                    System.out.println("요청 한전 API URL :" + useUrl);
                                    ResponseEntity<String> minuteUseApiResponse = restTemplate.exchange(useURI, HttpMethod.GET, entity, String.class);
                                    try {

                                        System.out.println("한전 15분 데이터 조회 결과 --> " + minuteUseApiResponse.toString());
                                        JsonObject jsonObject = gson.fromJson(minuteUseApiResponse.getBody(), JsonObject.class);
                                        JsonArray jsonArray = jsonObject.getAsJsonArray("minuteLpDataInfoList");
                                        jsonObject = gson.fromJson(jsonArray.get(0), JsonObject.class);
                                        ami.setMeteringVal(jsonObject.get("pwr_qty").toString());
                                        ami.setAccmltMeteringVal(jsonObject.get("vld_pwr").toString());
                                    } catch (JsonSyntaxException | ClassCastException e) {
                                        System.out.println("minuteUseApiResponse - 15분 단위 전력소비 데이터 JsonSyntaxException | ClassCastException");
                                        System.out.println("요청 URI " + useUrl);
                                        System.out.println(minuteUseApiResponse.toString());
                                    } catch (Exception e) {
                                        System.out.println("전력소비 데이터 Exception 발생");
                                        System.out.println(e.getLocalizedMessage());
                                    }

                                    //저압은 정각데이터만 제공됨.
                                } else {
                                    ami.setMeteringVal("0");
                                }
                                System.out.println("amiList : " + amiList);
                            }
                            mainService.insertAmiData(amiList);
                        }
                    } else {
                        System.out.println("한전 AMI 데이터와 매칭되는 데이터가 없습니다.");
                    }
                } else {
                    System.out.println("고객/계기번호 조회에 실패하였습니다.");
                    System.out.println(String.valueOf(meterApiResponse));
                }
            }
        } catch (DataIntegrityViolationException e) {
            System.out.println("기존에 등록된 한전 AMI 데이터입니다.");
            System.out.println(e.getLocalizedMessage());
        } catch (HttpServerErrorException e) {
            System.out.println("한전 데이터 수신 실패.");
            System.out.println(e.getLocalizedMessage());
        } catch (Exception e){
            System.out.println("한전 데이터 Exception 발생.");
            System.out.println(e.getLocalizedMessage());
        }
    }

    /**
     * getMinute(hhmm)
     * 현재 시간을 기준으로 15분 단위의 분을 구함
     * 00 ~ 14 : return "00"
     * 15 ~ 29 : return "15"
     * 30 ~ 44 : return "30"
     * 45 ~ 59 : return "45"
     **/
    public String getMinute(String hhmm){

        int time = Integer.parseInt(hhmm);
        String minute = "";

        if(time < 15)       minute = "00";
        else if (time < 30) minute = "15";
        else if (time < 45) minute = "30";
        else                minute = "45";

        return  minute;
    }










    //data.go.kr 인증키
    private String weatherApiServiceKey = "aDVsltIrJTOtDLpTA6qnVPhVhaT%2FaciIUGI30aiipGikIAAZOI4KxfVFBqW9q3s%2B3xgVzKx6c3gJdUVGaNJ9Bg%3D%3D";
    //공공데이터포털 응답 형식
    private String dataType = "JSON";
    //페이지 번호
    private String pageNo = "1";
    //인코딩 타입
    private String encoding = "UTF-8";
    //초단기 예보 API URL
    private String weatherForecastApiUrl = "http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getUltraSrtFcst";
    //페이지 결과 수 - 날씨
    private String weatherNumOfRows = "60";
    //초단기 예보 지역 코드 - 제주
    private String nx = "53";
    private String ny = "38";
    //일몰 일출 API URL
    private String sunriseSunsetApiUrl = "http://apis.data.go.kr/B090041/openapi/service/RiseSetInfoService/getAreaRiseSetInfo";
    //일몰 일출 API 지역
    private String sunsetSunriseApiLocation = "제주";
    //미세먼지 API URL
    private String fineDustApiUrl = "http://apis.data.go.kr/B552584/ArpltnInforInqireSvc/getMsrstnAcctoRltmMesureDnsty";
    //페이지 결과 수 - 미세먼지
    private String fineDustNumOfRows = "1";
    //미세 먼지 API 측정소
    private String fineDustStationName = "이도동";
    //미세 먼지 API 측정 주기
    private String fineDustDataTerm = "DAILY";
    //미세 먼지 API 버전
    private String fineDustVersion = "1.0";


    /**
     * getWeatherData()
     * 매 시간 10분에 날씨 관련 API 호출 하여 응답값 DB에 저장
     * 실제 반영 시에는 주석을 해제하여 반영
     **/
    @Scheduled(cron="0 10 0/1 * * *" )
    //@Scheduled(cron="*/10 * * * * *" )
    public void getWeatherData() throws URISyntaxException, UnsupportedEncodingException {

        System.out.println("[ ------------------------------------------------- ]");
        System.out.println("[ " + new SimpleDateFormat("yyyy년 MM월 dd일 HH:mm:ss").format(new Date()) + " - 날씨 Scheduler 실행 ====> ]");
        System.out.println("[ ------------------------------------------------- ]");

        RestTemplate restTemplate = new RestTemplate();
        final HttpEntity entity = new HttpEntity(new HttpHeaders());

        WeatherInterface weatherInterface = new WeatherInterface();
        /* 초단기 예보 데이터 START */
        URI weatherResponseURI = new URI(makeFullURI(weatherForecastApiUrl));

        ResponseEntity<String> weatherApiResponse = restTemplate.exchange(weatherResponseURI, HttpMethod.GET, entity, String.class);

        try {
            // 포맷 정의
            SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH");
            Date date = new Date();

            //서버 에러 발생시, 측정일과 측정시간은 담아주기 위함.
            String fcstDate = format.format(date);
            String fcstTime = timeFormat.format(date) + "00";
            weatherInterface.setNx(Integer.parseInt(nx));
            weatherInterface.setNy(Integer.parseInt(ny));
            weatherInterface.setBaseDate(fcstDate);
            weatherInterface.setBaseTime(fcstTime);

            System.out.println("weatherApiResponse : " +  weatherApiResponse);

            if(!Objects.requireNonNull(weatherApiResponse.getBody()).contains("html")) {
                JsonObject weatherResult = getApiResultBody(weatherApiResponse);

                if (weatherResult != null && weatherResult.size() > 0) {
                    JsonObject items = weatherResult.getAsJsonObject("items");
                    JsonArray item = items.getAsJsonArray("item");

                    for (int i = 0; i < item.size(); i++) {
                        JsonObject indexItem = (JsonObject) item.get(i);
                        /*
                         * - 자료구분 코드 정보
                         *    T1H - 기온 (단위: ℃)
                         *    SKY - 하늘상태 (맑음, 구름많음, 흐림)
                         *    REH - 습도 (단위: %)
                         *    PCP - 1시간 강수량
                         *    WSD - 풍속 (단위: m/s)
                         */
                        if (indexItem.get("fcstTime").toString().contains(fcstTime)) {
                            String category = indexItem.get("category").toString().replace("\"", "");

                            switch (category) {
                                case "T1H":
                                    weatherInterface.setTemp(indexItem.get("fcstValue").getAsInt());
                                    break;
                                case "SKY":
                                    weatherInterface.setSky(Integer.parseInt(indexItem.get("fcstValue").toString().replace("\"", "")));
                                    break;
                                case "REH":
                                    weatherInterface.setReh(indexItem.get("fcstValue").getAsString());
                                    break;
                                case "PCP":
                                    weatherInterface.setRn1(Integer.parseInt(indexItem.get("fcstValue").toString().replace("\"", "")));
                                    break;
                                case "WSD":
                                    weatherInterface.setWsd(indexItem.get("fcstValue").toString().replace("\"", ""));
                                    break;
                                default:
                                    break;
                            }
                        }
                    }
                }
            }
        } catch (NullPointerException | JsonSyntaxException | ClassCastException e) {
            System.out.println("weatherApiResponse - 초단기 예보 데이터 JsonSyntaxException/ClassCastException/NullPointerException");
            System.out.println("요청 URI :" + weatherResponseURI.toString());
            System.out.println(weatherApiResponse.toString());
        }
        /* 초단기 예보 데이터 END */

        /* 일출,일몰 데이터 START */
        URI sunsetSunriseResponseURI = new URI(makeFullURI(sunriseSunsetApiUrl));

        ResponseEntity<String> sunriseSunsetApiResponse = restTemplate.exchange(sunsetSunriseResponseURI, HttpMethod.GET, entity, String.class);
        try {

            System.out.println("sunriseSunsetApiResponse : " +  sunriseSunsetApiResponse);

            if(!Objects.requireNonNull(sunriseSunsetApiResponse.getBody()).contains("html")) {
                JsonObject sunriseSunsetResult = getApiResultBody(sunriseSunsetApiResponse);
                if (sunriseSunsetResult != null && sunriseSunsetResult.size() > 0) {
                    JsonObject items = (JsonObject) sunriseSunsetResult.get("items");
                    if (items.size() > 0) {
                        JsonObject item = items.getAsJsonObject("item");
                        String sunrise = item.get("sunrise").toString();
                        String sunset = item.get("sunset").toString();
                        weatherInterface.setSunrise(sunrise.substring(1, 3) + ":" + sunrise.substring(3, 5));
                        weatherInterface.setSunset(sunset.substring(1, 3) + ":" + sunset.substring(3, 5));
                    }
                }
            }
        } catch (NullPointerException | JsonSyntaxException | ClassCastException e) {
            System.out.println("sunriseSunsetApiResponse - 일몰 일출 데이터 JsonSyntaxException/ClassCastException/NullPointerException");
            System.out.println("요청 URI --> " + sunsetSunriseResponseURI.toString());
            System.out.println(sunriseSunsetApiResponse.toString());
        }
        /* 일출,일몰 데이터 END */

        /* 미세 먼지 데이터 START */
        URI fineDustResponseURI = new URI(makeFullURI(fineDustApiUrl));

        ResponseEntity<String> fineDustApiResponse = restTemplate.exchange(fineDustResponseURI, HttpMethod.GET, entity, String.class);

        try {

            System.out.println("fineDustApiResponse : " +  fineDustApiResponse);

            if(!Objects.requireNonNull(fineDustApiResponse.getBody()).contains("html")) {
                JsonObject fineDustResult = getApiResultBody(fineDustApiResponse);
                if (fineDustResult != null && fineDustResult.size() > 0) {
                    JsonArray items = (JsonArray) fineDustResult.get("items");
                    if (items.size() > 0) {
                        JsonObject item = (JsonObject) items.get(0);
                        weatherInterface.setPm10(item.get("pm10Flag").isJsonNull() ? item.get("pm10Value").toString().replace("\"", "") : "");
                    }
                }
            }
        } catch (NullPointerException | JsonSyntaxException | ClassCastException e) {
            System.out.println("fineDustApiResponse - 미세 먼지 데이터 JsonSyntaxException/ClassCastException");
            System.out.println("요청 URL : " + fineDustResponseURI.toString());
            System.out.println(fineDustApiResponse.toString());
        }


        try {
            /* 미세 먼지 데이터 END */
            mainService.insertWeatherData(weatherInterface);
        } catch (Exception e) {
            System.out.println("미세 먼지 데이터 저장 오류");
            System.out.println("weatherInterface : " + weatherInterface);
            System.out.println(e.getLocalizedMessage());
        }

    }

    /**
     * getApiResultBody(ResponseEntity<String> apiResponse)
     * API 파싱시 items가 JSONObject / JSONArray 일수 있으므로 요청 성공시 body까지만 가져온다.
     **/
    public JsonObject getApiResultBody(ResponseEntity<String> apiResponse){

        JsonObject apiResultBody = new JsonObject();

        if (apiResponse.getStatusCode() == HttpStatus.OK) {
            JsonObject jsonObject = gson.fromJson(apiResponse.getBody(), JsonObject.class);
            jsonObject = jsonObject.getAsJsonObject("response");
            apiResultBody = jsonObject.getAsJsonObject("body");
        }

        return apiResultBody;
    }

    /**
     * makeFullURI(String apiURL)
     * data.go.kr API에 따라 URI로 만들 파라미터 세팅
     **/
    public String makeFullURI(String apiURL) throws UnsupportedEncodingException {

        /* URL */
        StringBuilder urlBuilder = new StringBuilder(apiURL);
        /* API service KEY */
        urlBuilder.append("?" + URLEncoder.encode("serviceKey", "UTF-8") + "=" + weatherApiServiceKey);

        if(apiURL.contains("getUltraSrtFcst")){                         /* 초단기 날씨 예보 */

            Calendar calendar =  Calendar.getInstance();
            // 조회시엔 현재 시간 -30분
            calendar.add(Calendar.MINUTE, -30);
            Date date = new Date(calendar.getTimeInMillis());

            // 포맷 정의
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH");
            SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");

            String baseDate = format.format(date);
            String baseTime = timeFormat.format(date) + "30";

            urlBuilder.append("&" + URLEncoder.encode("numOfRows", "UTF-8") + "=" + URLEncoder.encode(weatherNumOfRows, encoding));              // 페이지 번호
            urlBuilder.append("&" + URLEncoder.encode("pageNo", "UTF-8") + "=" + URLEncoder.encode(pageNo, encoding));                           // 한 페이지 결과 수
            urlBuilder.append("&" + URLEncoder.encode("dataType", "UTF-8") + "=" + URLEncoder.encode(dataType, encoding));                       // 데이터 타입
            urlBuilder.append("&" + URLEncoder.encode("base_date", "UTF-8") + "=" + URLEncoder.encode(baseDate, encoding));                      // 날짜
            urlBuilder.append("&" + URLEncoder.encode("base_time", "UTF-8") + "=" + URLEncoder.encode(baseTime, encoding));                      // 시간
            urlBuilder.append("&" + URLEncoder.encode("nx", "UTF-8") + "=" + URLEncoder.encode(nx, encoding));                                   // x 좌표
            urlBuilder.append("&" + URLEncoder.encode("ny", "UTF-8") + "=" + URLEncoder.encode(ny, encoding));                                   // Y 좌표

        } else if(apiURL.contains("getAreaRiseSetInfo")) {              /* 일출,일몰 시간 */
            // 현재 날짜 구하기
            Date date = new Date();
            // 포맷 정의
            SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
            String locdate = format.format(date);

            urlBuilder.append("&" + URLEncoder.encode("location", "UTF-8") + "=" + URLEncoder.encode(sunsetSunriseApiLocation, encoding));       // 지역명
            urlBuilder.append("&" + URLEncoder.encode("locdate", "UTF-8") + "=" + URLEncoder.encode(locdate, encoding));                         // 날짜

        } else if(apiURL.contains("getMsrstnAcctoRltmMesureDnsty")){    /* 미세 먼지 */
            urlBuilder.append("&" + URLEncoder.encode("returnType","UTF-8") + "=" + URLEncoder.encode(dataType, encoding));                      // 데이터 타입
            urlBuilder.append("&" + URLEncoder.encode("numOfRows","UTF-8") + "=" + URLEncoder.encode(fineDustNumOfRows, encoding));              // 한 페이지 결과 수
            urlBuilder.append("&" + URLEncoder.encode("pageNo","UTF-8") + "=" + URLEncoder.encode(pageNo, encoding));                            // 페이지 번호
            urlBuilder.append("&" + URLEncoder.encode("stationName","UTF-8") + "=" + URLEncoder.encode(fineDustStationName, encoding));          // 측정소 이름
            urlBuilder.append("&" + URLEncoder.encode("dataTerm","UTF-8") + "=" + URLEncoder.encode(fineDustDataTerm, encoding));                // 요청 데이터 기간
            urlBuilder.append("&" + URLEncoder.encode("ver","UTF-8") + "=" + URLEncoder.encode(fineDustVersion, encoding));                      // 버전별 상세 결과 참고

        }

        return urlBuilder.toString();
    }
}

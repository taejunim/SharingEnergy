package SharingEnergy.Object;

import lombok.Getter;
import lombok.Setter;

/**
 * WeatherInterface.java
 *
 * ESS 정보
 *
 * Created by Youyeong Jo on 2022/04/15.
 */

@Getter
@Setter
public class WeatherInterface {

    private String regDt;       //등록일자
    private String baseDate;    //예보일자
    private String baseTime;    //예보시간
    private int nx;             //지역 코드 x
    private int ny;             //지역 코드 y
    private int sky;            //하늘 상태
    private int temp;           //온도
    private String reh;         //습도
    private int rn1;            //1시간 강수량
    private String wsd;         //풍속
    private String sunrise;     //일출 시간
    private String sunset;      //일몰 시간
    private String pm10;        //미세 먼지 (pm10)
}

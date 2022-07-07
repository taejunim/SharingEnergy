package SharingEnergy.Object;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class KepcoAmi {

    private String custNo;              //한전고객번호
    private String meterNo;             //계기번호 (mid)
    @SerializedName("lv_hv_val")
    private String lvHvVal;             //저/고압 구분
    private String accmltMeteringVal;   //유효전력(kWh) - 누적치, 저압만 제공
    private String meteringVal;         //15분 단위 전력량
    private String amiIdx;              //AMI ID (CEMS)
    private String meteringDate;        //검침일자
    private String meteringTime;        //검침시간

}

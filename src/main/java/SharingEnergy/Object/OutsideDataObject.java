package SharingEnergy.Object;

import lombok.Data;

@Data
public class OutsideDataObject {

    private String deviceId;
    private String deviceGbnCd;
    private String chgrGbnCd;
    private String measureDttm;
    private double actElecPwr;
    private double actElecEnergy;
    private String orgGbnCd;
    private String orgCd;
}


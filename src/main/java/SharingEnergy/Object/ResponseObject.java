package SharingEnergy.Object;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.ArrayList;

@Data
public class ResponseObject {

    public String code;
    public String result;
    public String message;
    public ArrayList<OutsideDataObject> outsideDataList;

    @JsonIgnore
    public String dataList;
}

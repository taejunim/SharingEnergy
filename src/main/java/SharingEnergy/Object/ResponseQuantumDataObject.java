package SharingEnergy.Object;

import lombok.Data;

import java.util.ArrayList;

@Data
public class ResponseQuantumDataObject {

    public String result;
    public String message;
    public ArrayList<OutsideDataObject> outsideDataList;
}

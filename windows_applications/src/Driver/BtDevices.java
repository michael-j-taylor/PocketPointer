package Driver;

public class BtDevices {
    private String devName, devBtId;

    public String getDevName() {
        return devName;
    }

    public void setDevName(String devName) {
        this.devName = devName;
    }

    public String getDevBtId() {
        return devBtId;
    }

    public void setDevBtId(String devBtId) {
        this.devBtId = devBtId;
    }

    public BtDevices(String name, String btId) {
        devName = name;
        devBtId = btId;
    }


}

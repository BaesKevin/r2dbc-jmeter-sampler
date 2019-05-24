package be.kevinbaes.bap.jmetersampler.domain;

import java.util.Date;

/**
 * Entity used to test selects
 */
public class DeviceEvent {

  private int id;
  private Date receivedTime;
  private long lattitude;
  private long longitude;

  public DeviceEvent(int id, Date receivedTime, long lattitude, long longitude) {
    this.id = id;
    this.receivedTime = receivedTime;
    this.lattitude = lattitude;
    this.longitude = longitude;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public Date getReceivedTime() {
    return receivedTime;
  }

  public void setReceivedTime(Date receivedTime) {
    this.receivedTime = receivedTime;
  }

  public long getLattitude() {
    return lattitude;
  }

  public void setLattitude(long lattitude) {
    this.lattitude = lattitude;
  }

  public long getLongitude() {
    return longitude;
  }

  public void setLongitude(long longitude) {
    this.longitude = longitude;
  }
}

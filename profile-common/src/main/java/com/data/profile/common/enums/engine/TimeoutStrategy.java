package com.data.profile.common.enums.engine;

//import com.baomidou.mybatisplus.annotation.EnumValue;

/**
 * failure policy when some task node failed.
 */
public enum TimeoutStrategy {

  /**
   * 0 ending process when some tasks failed.
   * 1 continue running when some tasks failed.
   **/
  RETRY(0, "retry"),
  WARN(1, "warn");

  TimeoutStrategy(int code, String description){
    this.code = code;
    this.description = description;
  }

  //@EnumValue
  final int code;

  final String description;

  public int getCode() {
    return code;
  }

  public String getDescription() {
    return description;
  }
}

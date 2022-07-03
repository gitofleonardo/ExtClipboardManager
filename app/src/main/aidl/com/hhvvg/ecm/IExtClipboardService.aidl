// IExtClipboardService.aidl
package com.hhvvg.ecm;

import com.hhvvg.ecm.configuration.AutoClearStrategyInfo;

interface IExtClipboardService {

    void setEnable(boolean enable);
    boolean isEnable();

    void setAutoClearEnable(boolean enable);
    boolean isAutoClearEnable();
    int getAutoClearWorkMode();
    void setAutoClearWorkMode(int mode);
    int getAutoClearReadCount();
    void setAutoClearReadCount(int count);
    void setAutoClearAppWhitelist(in List<String> exclusions);
    void setAutoClearAppBlacklist(in List<String> exclusions);
    List<String> getAutoClearAppBlacklist();
    List<String> getAutoClearAppWhitelist();
    void setAutoClearContentExclusionList(in List<String> exclusions);
    List<String> getAutoClearContentExclusionList();

    void setAutoClearTimeout(long timeout);
    long getAutoClearTimeout();

    List<AutoClearStrategyInfo> getAutoClearStrategies();
    void addAutoClearStrategy(in AutoClearStrategyInfo strategy);
    void removeStrategy(String packageName);
}

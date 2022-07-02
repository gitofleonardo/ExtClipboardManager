// IExtClipboardService.aidl
package com.hhvvg.ecm;

// Declare any non-default types here with import statements

import com.hhvvg.ecm.configuration.AutoClearStrategyInfo;

interface IExtClipboardService {

    void setEnable(boolean enable);
    boolean isEnable();

    void setAutoClearEnable(boolean enable);
    boolean isAutoClearEnable();
    void setAutoClearTimeout(long timeout);
    long getAutoClearTimeout();

    List<AutoClearStrategyInfo> getAutoClearStrategies();
    void addAutoClearStrategy(in AutoClearStrategyInfo strategy);
    void removeStrategy(String packageName);
}

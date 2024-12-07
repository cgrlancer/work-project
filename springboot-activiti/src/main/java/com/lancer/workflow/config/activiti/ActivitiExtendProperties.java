package com.lancer.workflow.config.activiti;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

//@Data
@ConfigurationProperties(prefix = "spring.activiti.xboot")
public class ActivitiExtendProperties {

    /**
     * 流程图字体配置
     */
    private String activityFontName = "Microsoft YaHei UI";

    /**
     * 流程图字体配置
     */
    private String labelFontName = "Microsoft YaHei UI";

    public String getLabelFontName() {
        return labelFontName;
    }

    public void setLabelFontName(String labelFontName) {
        this.labelFontName = labelFontName;
    }

    public String getActivityFontName() {
        return activityFontName;
    }

    public void setActivityFontName(String activityFontName) {
        this.activityFontName = activityFontName;
    }
}

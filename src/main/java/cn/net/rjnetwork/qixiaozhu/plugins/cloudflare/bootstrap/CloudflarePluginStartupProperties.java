package cn.net.rjnetwork.qixiaozhu.plugins.cloudflare.bootstrap;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "eqadmin.cloudflare.startup")
public class CloudflarePluginStartupProperties {

    private boolean enabled = true;

    private boolean failFast = true;

    private boolean staticSyncEnabled = true;

    private boolean staticSyncRequired = false;

    private String staticClasspathRoot = "eqadmin-cloudflare/web/childrens/cloudflare";

    private String staticSourceDir = "";

    private String targetChildDirName = "cloudflare";

    private String backupDirName = "_backup";

    private boolean microappSyncEnabled = true;

    private String microappEnname = "eqadminPluginsCloudflare";

    private String microappZhname = "边缘云平台";

    private String microappBaseUrl = "/childrens/cloudflare";

    private Integer microappTimeout = 500;

    private Integer microappIframe = 0;

    private String microappContainer = "MICROAPP-DIV-ID";

    private Integer microappStatus = 1;

    private String microappSchem = "";

    private Long microappCreateUserId = 1L;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isFailFast() {
        return failFast;
    }

    public void setFailFast(boolean failFast) {
        this.failFast = failFast;
    }

    public boolean isStaticSyncEnabled() {
        return staticSyncEnabled;
    }

    public void setStaticSyncEnabled(boolean staticSyncEnabled) {
        this.staticSyncEnabled = staticSyncEnabled;
    }

    public boolean isStaticSyncRequired() {
        return staticSyncRequired;
    }

    public void setStaticSyncRequired(boolean staticSyncRequired) {
        this.staticSyncRequired = staticSyncRequired;
    }

    public String getStaticClasspathRoot() {
        return staticClasspathRoot;
    }

    public void setStaticClasspathRoot(String staticClasspathRoot) {
        this.staticClasspathRoot = staticClasspathRoot;
    }

    public String getStaticSourceDir() {
        return staticSourceDir;
    }

    public void setStaticSourceDir(String staticSourceDir) {
        this.staticSourceDir = staticSourceDir;
    }

    public String getTargetChildDirName() {
        return targetChildDirName;
    }

    public void setTargetChildDirName(String targetChildDirName) {
        this.targetChildDirName = targetChildDirName;
    }

    public String getBackupDirName() {
        return backupDirName;
    }

    public void setBackupDirName(String backupDirName) {
        this.backupDirName = backupDirName;
    }

    public boolean isMicroappSyncEnabled() {
        return microappSyncEnabled;
    }

    public void setMicroappSyncEnabled(boolean microappSyncEnabled) {
        this.microappSyncEnabled = microappSyncEnabled;
    }

    public String getMicroappEnname() {
        return microappEnname;
    }

    public void setMicroappEnname(String microappEnname) {
        this.microappEnname = microappEnname;
    }

    public String getMicroappZhname() {
        return microappZhname;
    }

    public void setMicroappZhname(String microappZhname) {
        this.microappZhname = microappZhname;
    }

    public String getMicroappBaseUrl() {
        return microappBaseUrl;
    }

    public void setMicroappBaseUrl(String microappBaseUrl) {
        this.microappBaseUrl = microappBaseUrl;
    }

    public Integer getMicroappTimeout() {
        return microappTimeout;
    }

    public void setMicroappTimeout(Integer microappTimeout) {
        this.microappTimeout = microappTimeout;
    }

    public Integer getMicroappIframe() {
        return microappIframe;
    }

    public void setMicroappIframe(Integer microappIframe) {
        this.microappIframe = microappIframe;
    }

    public String getMicroappContainer() {
        return microappContainer;
    }

    public void setMicroappContainer(String microappContainer) {
        this.microappContainer = microappContainer;
    }

    public Integer getMicroappStatus() {
        return microappStatus;
    }

    public void setMicroappStatus(Integer microappStatus) {
        this.microappStatus = microappStatus;
    }

    public String getMicroappSchem() {
        return microappSchem;
    }

    public void setMicroappSchem(String microappSchem) {
        this.microappSchem = microappSchem;
    }

    public Long getMicroappCreateUserId() {
        return microappCreateUserId;
    }

    public void setMicroappCreateUserId(Long microappCreateUserId) {
        this.microappCreateUserId = microappCreateUserId;
    }
}

/*
 * Hello Minecraft!.
 * Copyright (C) 2013  huangyuhui <huanghongxun2008@126.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see {http://www.gnu.org/licenses/}.
 */
package org.jackhuang.hellominecraft.util;

import org.jackhuang.hellominecraft.util.logging.HMCLog;
import java.util.Map;

/**
 *
 * @author huangyuhui
 */
public final class UpdateChecker implements IUpdateChecker {
	
	public static final String VERSION_URL = "http://client.api.mcgogogo.com:81/version.php?type=";
	public static final String UPDATE_LINK_URL = "http://client.api.mcgogogo.com:81/update_link.php?type=";
	
    public boolean OUT_DATED = false;
	
	public String versionString;
    public VersionNumber base;
    private VersionNumber value;
	private boolean isforceUpdate = false;
	private boolean isManualUpdate = false;
	
    public String type;
    private Map<String, String> download_link = null;

    public UpdateChecker(VersionNumber base, String type) {
        this.base = base;
        this.type = type;
    }

    @Override
    public OverridableSwingWorker<VersionNumber> process(final boolean showMessage) {
        return new OverridableSwingWorker() {
            @Override
            protected void work() throws Exception {
				isManualUpdate = showMessage;
		
                if (value == null) {
                    versionString = NetUtils.get(VERSION_URL + type + "&ver=" + base.toString());
					Map<String, Object> versionInfo = C.GSON.fromJson(versionString, Map.class);
					if (versionInfo.containsKey("version"))
						value = VersionNumber.check((String)versionInfo.get("version"));
					if (versionInfo.containsKey("force"))
						isforceUpdate = (boolean)versionInfo.get("force");
                }
				
                if (value == null) {
                    HMCLog.warn("Failed to check update...");
                    if (showMessage) {
                        MessageBox.Show(C.i18n("update.failed"));
					}
                } else if (VersionNumber.isOlder(base, value)) {
                    OUT_DATED = true;
				}
				
                if (OUT_DATED) {
                    publish(value);
				}
            }
        };
    }

    @Override
    public VersionNumber getNewVersion() {
        return value;
    }

	@Override
	public boolean isForceUpdate() {
		return isforceUpdate;
	}

	@Override
	public boolean isManualUpdate() {
		return isManualUpdate;
	}

    @Override
    public synchronized OverridableSwingWorker<Map<String, String>> requestDownloadLink() {
        return new OverridableSwingWorker() {
            @Override
            protected void work() throws Exception {
                if (download_link == null)
                    try {
                        download_link = C.GSON.fromJson(NetUtils.get(UPDATE_LINK_URL + type), Map.class);
                    } catch (Exception e) {
                        HMCLog.warn("Failed to get update link.", e);
                    }
                publish(download_link);
            }
        };
    }

    public final EventHandler<VersionNumber> outdated = new EventHandler<>(this);

    @Override
    public void checkOutdate() {
        if (OUT_DATED)
            outdated.execute(getNewVersion());
    }
}

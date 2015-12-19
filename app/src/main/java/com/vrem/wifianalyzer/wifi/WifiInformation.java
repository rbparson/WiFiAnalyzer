/*
 *    Copyright (C) 2010 - 2015 VREM Software Development <VREMSoftwareDevelopment@gmail.com>
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.vrem.wifianalyzer.wifi;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class WiFiInformation {
    private final Connection connection;
    private final List<DetailsInfo> detailsInfoList = new ArrayList<>();
    private final List<WiFiRelationship> wifiRelationships = new ArrayList<>();

    public WiFiInformation() {
        this(null, null);
    }

    public WiFiInformation(List<ScanResult> scanResults, WifiInfo wifiInfo) {
        connection = new Connection(wifiInfo);
        if (scanResults != null) {
            for (ScanResult scanResult : scanResults) {
                DetailsInfo detailsInfo = new Details(scanResult);
                if (!connection.addDetailsInfo(detailsInfo)) {
                    detailsInfoList.add(detailsInfo);
                }
            }
            populateRelationship();
        }
    }

    private void populateRelationship() {
        Collections.sort(detailsInfoList, new SSIDComparator());
        WiFiRelationship wifiRelationship = null;
        for (DetailsInfo detailsInfo : detailsInfoList) {
            if (wifiRelationship == null || !wifiRelationship.getParent().getSSID().equals(detailsInfo.getSSID())) {
                wifiRelationship = new WiFiRelationship(detailsInfo);
                wifiRelationships.add(wifiRelationship);
            } else {
                wifiRelationship.addChild(detailsInfo);
            }
        }
        Collections.sort(wifiRelationships);
    }

    public int getParentsSize() {
        return wifiRelationships.size();
    }

    public DetailsInfo getParent(int index) {
        return wifiRelationships.get(index).getParent();
    }

    public int getChildrenSize(int index) {
        return wifiRelationships.get(index).getChildrenSize();
    }

    public DetailsInfo getChild(int indexParent, int indexChild) {
        return wifiRelationships.get(indexParent).getChild(indexChild);
    }

    public Connection getConnection() {
        return connection;
    }

    class SSIDComparator implements Comparator<DetailsInfo> {
        @Override
        public int compare(DetailsInfo lhs, DetailsInfo rhs) {
            int result = lhs.getSSID().compareTo(rhs.getSSID());
            if (result == 0) {
                result = lhs.getLevel() - rhs.getLevel();
                if (result == 0) {
                    result = lhs.getBSSID().compareTo(rhs.getBSSID());
                }
            }
            return result;
        }
    }

}

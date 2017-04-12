package com.circletech.smartconnect;

import com.circletech.smartconnect.data.DeviceDistanceData;
import com.circletech.smartconnect.model.DeviceBadDistance;
import com.circletech.smartconnect.model.DeviceDistance;
import com.circletech.smartconnect.service.DeviceBadDistanceService;
import com.circletech.smartconnect.service.DeviceDistanceService;
import com.circletech.smartconnect.util.ConstantUtil;
import com.circletech.smartconnect.util.DeviceDistanceUtil;
import com.circletech.smartconnect.util.Vec3DUtil;
import com.circletech.smartconnect.util.Vec3DUtilResult;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by xieyingfei on 2017/1/8.
 */
public class DataTag {

    private Map<String, List<Long> > tagMap ;
    private ReadWriteLock rwl = new ReentrantReadWriteLock();

    public DataTag() {
        tagMap = new ConcurrentHashMap<String,List<Long> >();
    }

    public String getTag(Long baseId, Long deviceId, Long timestamp){
        rwl.writeLock().lock();
        try {
            //Long mintimespan = (long)0;
            Long insertTime = timestamp;
            String insertKey = "";

            for (Map.Entry<String, List<Long> > entry : tagMap.entrySet()) {

                List<Long> temp = entry.getValue().subList(2, entry.getValue().size());

                boolean b1 = entry.getValue().get(1).equals(deviceId);
                boolean b2 = !temp.contains(baseId);
                long timespan = Math.abs(timestamp.longValue() - entry.getValue().get(0).longValue());
                //boolean b3 = (mintimespan.equals((long)0) ||timespan <= mintimespan.longValue());
                boolean b3 = (timestamp.longValue() == entry.getValue().get(0).longValue());

                if( b1 && b2 && b3){
                        //mintimespan = timespan;
                        insertTime = entry.getValue().get(0);
                        insertKey = entry.getKey();
                }
            }

            if(!insertKey.equals("")){
                tagMap.get(insertKey).add(baseId);
                return insertKey;
            }else{

                String newTag = UUID.randomUUID().toString();
                List<Long> bases = new ArrayList<Long>();
                bases.add(insertTime);//列表的第一个值保证是时间戳
                bases.add(deviceId);//属于哪个标签
                bases.add(baseId);//与哪个基站相距
                tagMap.put(newTag, bases);

                return newTag;
            }

        }finally {
            rwl.writeLock().unlock();
        }
    }

    public List<Vec3DUtilResult> getTagResults(Long deviceId, DeviceDistanceData deviceDistanceData, DeviceDistanceService deviceDistanceService, Long taskTimeStamp, DeviceBadDistanceService deviceBadDistanceService) {
        rwl.writeLock().lock();
        try {
            List<Vec3DUtilResult> lstV = new ArrayList<Vec3DUtilResult>();

            for (String id : tagMap.keySet()) {

                boolean b1 = tagMap.get(id).get(1).equals(deviceId);
                if(b1 && tagMap.get(id).size() == 6){

                    List<DeviceDistanceUtil> devicelist = deviceDistanceData.findList(id);

                    if(devicelist.size() == 4){
                        Vec3DUtil result = Vec3DUtil.trilateration4p(
                                devicelist.get(0).getVec3DUtil(), (double)devicelist.get(0).getDistance(),
                                devicelist.get(1).getVec3DUtil(), (double)devicelist.get(1).getDistance(),
                                devicelist.get(2).getVec3DUtil(), (double)devicelist.get(2).getDistance(),
                                devicelist.get(3).getVec3DUtil(), (double)devicelist.get(3).getDistance(),
                                0.01
                        );

                        if(result != null){
                            //有效的距离数据
                            for(int i = 0; i < 4; i++){
                                deviceDistanceService.save(new DeviceDistance(devicelist.get(i).getBaseId(), devicelist.get(i).getDeviceId(),devicelist.get(i).getDistance(),new Timestamp(taskTimeStamp),new Timestamp(System.currentTimeMillis())));
                            }
                            lstV.add(new Vec3DUtilResult(result, tagMap.get(id).get(0)));
                        }else{
                            //无效的距离数据
                            for(int i = 0; i < 4; i++){
                                deviceBadDistanceService.save(new DeviceBadDistance(devicelist.get(i).getBaseId(), devicelist.get(i).getDeviceId(),devicelist.get(i).getDistance(),new Timestamp(taskTimeStamp),new Timestamp(System.currentTimeMillis())));
                            }

                        }

                        tagMap.remove(id);
                        deviceDistanceData.removeList(id);
                    }
                }
            }

            return lstV;
        }finally {
           rwl.writeLock().unlock();
        }
    }

    public void removeTag(){
        rwl.writeLock().lock();

        try{
            for (String id : tagMap.keySet()) {
                List<Long> templist = tagMap.get(id);
                long timespan = System.currentTimeMillis() - templist.get(0).longValue();
                if(templist.size() < 6 && timespan > 0 && timespan >= ConstantUtil.CLEAR_LOSS_DISTANCE_DATA_SPAN){
                    tagMap.remove(id);
                }
            }
        }finally {
             rwl.writeLock().unlock();
        }
    }
}

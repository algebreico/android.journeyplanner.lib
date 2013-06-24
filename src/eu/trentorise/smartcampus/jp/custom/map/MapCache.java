package eu.trentorise.smartcampus.jp.custom.map;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import eu.trentorise.smartcampus.jp.model.SmartCheckStop;

public class MapCache {

	private Map <String, Set< SmartCheckStop>> cache = new HashMap<String, Set<SmartCheckStop>>();
	private static final String key= "agencyId";
	public Collection <SmartCheckStop> getStopsByAgencyIds(String[] ids){
		List<SmartCheckStop> list = new ArrayList<SmartCheckStop>();
		for (String id: ids){
			if (cache.containsKey(id))
				list.addAll(cache.get(id));
		}
		return list;	
	}
	
	public boolean addStop(SmartCheckStop stop)
	{
		String agencyId = (String) stop.getCustomData().get(key); 
		 Set<SmartCheckStop> cacheByAgencyId = cache.get(agencyId);
 		if (cacheByAgencyId == null) {
			cacheByAgencyId = new HashSet<SmartCheckStop>();
 			cache.put(agencyId, cacheByAgencyId);
		}
		return cacheByAgencyId.add(stop);
// 		
//		 if (cache.get(agencyId)!=null){
//			 cacheByAgencyId = cache.get(agencyId);	 
//		 } else {
//			 cacheByAgencyId = new HashMap<String, SmartCheckStop>();
//			 cache.put(agencyId, cacheByAgencyId);
//		 }
//		 if (cacheByAgencyId.get(stop).getId()==null)
//			 cacheByAgencyId.put(stop.getId(), stop);
	}
//	public void addStops (String agencyid, List<SmartCheckStop> stops){
//		if (cache.get(agencyid) == null) {
//			cache.put(agencyid, new HashSet<SmartCheckStop>());
//		}
//		cache.get(agencyid).addAll(stops);
////		for (SmartCheckStop stop:stops){
////			addStop(stop);
////		}
//	}
}

package org.magnum.dataup.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import javax.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class VideoRepository {

	private static final AtomicLong currentId = new AtomicLong(0L);
	private Map<Long, Video> videos = new HashMap<Long, Video>();
	private static VideoRepository instance = null;
	
	public Video save(Video entity) {
		checkAndSetId(entity);
		
		String dataUrl = getDataUrl(entity.getId());
		entity.setDataUrl(dataUrl);
		
		videos.put(entity.getId(), entity);
		
		return entity; 
	}

	private void checkAndSetId(Video entity) {
		if (entity.getId() == 0)
			entity.setId(currentId.incrementAndGet());
	}
	
	public Collection<Video> getVideos() {
		return videos.values();
	}
	
	private String getDataUrl(long videoId) {
		String url = getUrlBaseForLocalServer() + "/video/" + videoId + "/data";
		
		return url;
	}
	
	private String getUrlBaseForLocalServer() {
		HttpServletRequest request =
				((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
		
		String base = "http://" + request.getServerName()
				+ ((request.getServerPort() != 80) ? ":" + request.getServerPort() : "");
		
		return base;
	}
	
	private VideoRepository() {}
	public static VideoRepository getInstance() {
		if (instance == null)
			instance = new VideoRepository();
		
		return instance;
	}

	public Video getVideoById(long id) {
		return videos.get(id);
	}
}

/*
 * 
 * Copyright 2014 Jules White
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package org.magnum.dataup;

import java.io.IOException;
import java.util.Collection;

import javax.servlet.http.HttpServletResponse;

import org.magnum.dataup.model.Video;
import org.magnum.dataup.model.VideoRepository;
import org.magnum.dataup.model.VideoStatus;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class VideoController {

	/**
	 * You will need to create one or more Spring controllers to fulfill the
	 * requirements of the assignment. If you use this file, please rename it
	 * to something other than "AnEmptyController"
	 * 
	 * 
		 ________  ________  ________  ________          ___       ___  ___  ________  ___  __       
		|\   ____\|\   __  \|\   __  \|\   ___ \        |\  \     |\  \|\  \|\   ____\|\  \|\  \     
		\ \  \___|\ \  \|\  \ \  \|\  \ \  \_|\ \       \ \  \    \ \  \\\  \ \  \___|\ \  \/  /|_   
		 \ \  \  __\ \  \\\  \ \  \\\  \ \  \ \\ \       \ \  \    \ \  \\\  \ \  \    \ \   ___  \  
		  \ \  \|\  \ \  \\\  \ \  \\\  \ \  \_\\ \       \ \  \____\ \  \\\  \ \  \____\ \  \\ \  \ 
		   \ \_______\ \_______\ \_______\ \_______\       \ \_______\ \_______\ \_______\ \__\\ \__\
		    \|_______|\|_______|\|_______|\|_______|        \|_______|\|_______|\|_______|\|__| \|__|
                                                                                                                                                                                                                                                                        
	 * 
	 */
	private VideoRepository videoRepository;
	private VideoFileManager videoFileManager;
	
	public VideoController() {
		videoRepository = VideoRepository.getInstance();
		
		try {
			videoFileManager = VideoFileManager.get();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@RequestMapping(value=VideoSvcApi.VIDEO_SVC_PATH, method=RequestMethod.GET)
	public @ResponseBody Collection<Video> getVideoList() {
		return videoRepository.getVideos();
	}
	
	@RequestMapping(value=VideoSvcApi.VIDEO_SVC_PATH, method=RequestMethod.POST)
	public @ResponseBody Video addVideo(@RequestBody Video v) {
		Video video = videoRepository.save(v);
		
		return video;
	}
	
	@RequestMapping(value=VideoSvcApi.VIDEO_DATA_PATH, method=RequestMethod.POST)
	public @ResponseBody VideoStatus setVideoData(
			@PathVariable(VideoSvcApi.ID_PARAMETER) long id, 
			@RequestParam(VideoSvcApi.DATA_PARAMETER) MultipartFile videoData,
			HttpServletResponse response) throws IOException {

		Video video = videoRepository.getVideoById(id);

		if (video == null)
			response.setStatus(HttpStatus.NOT_FOUND.value());
		else {
			response.setStatus(HttpStatus.OK.value());
			videoFileManager.saveVideoData(video, videoData.getInputStream());
		}			
		
		return new VideoStatus(VideoStatus.VideoState.READY);
	}
	
	// Reference: http://java.dzone.com/articles/handling-spring-mvc-exceptions
//	@ExceptionHandler(Exception.class)
//	public @ResponseBody Map<String, String> errorResponse(Exception ex, HttpServletResponse response) {
//		Map<String, String> errorMap = new HashMap<String, String>();
//		errorMap.put("errorMessage", ex.getMessage());
//		
//		StringWriter sw = new StringWriter();
//		PrintWriter pw = new PrintWriter(sw);
//		
//		ex.printStackTrace(pw);
//		String stackTrace = sw.toString();
//		
//		errorMap.put("errorStacktrace", stackTrace);
//		response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
//		
//		return errorMap;		
//	}
	
	@RequestMapping(value=VideoSvcApi.VIDEO_DATA_PATH, method=RequestMethod.GET)
	public void getData(@PathVariable(VideoSvcApi.ID_PARAMETER) long id, 
			HttpServletResponse response) throws IOException {
		
		Video video = videoRepository.getVideoById(id);
		
		if (video == null)
			response.setStatus(HttpStatus.NOT_FOUND.value());
		else
			videoFileManager.copyVideoData(video, response.getOutputStream());
	}
}
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

package org.magnum.mobilecloud.video;

import java.io.Console;
import java.security.Principal;
import java.util.Collection;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.magnum.mobilecloud.video.client.VideoSvcApi;
import org.magnum.mobilecloud.video.repository.Video;
import org.magnum.mobilecloud.video.repository.VideoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.collect.Lists;

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
	
	@Autowired
	private VideoRepository videoRepo;
	
	@RequestMapping(value=VideoSvcApi.VIDEO_SVC_PATH, method=RequestMethod.GET)
	public @ResponseBody Collection<Video> getVideoList() {
		return Lists.newArrayList(videoRepo.findAll());
	}
	
	@RequestMapping(value=VideoSvcApi.VIDEO_SVC_PATH_ID, method=RequestMethod.GET)
	public @ResponseBody Video getVideoById(
			@PathVariable(VideoSvcApi.ID_PARAMETER) long id,
			HttpServletResponse response) {
		
		Video video = videoRepo.findOne(id);
		
		if (video == null)
			response.setStatus(HttpStatus.NOT_FOUND.value());
		
		return video;
	}
	
	@RequestMapping(value=VideoSvcApi.VIDEO_SVC_PATH, method=RequestMethod.POST)
	public @ResponseBody Video addVideo(@RequestBody Video v) {
		
		v.setLikes(0);		
		Video video = videoRepo.save(v);

		return video;
	}
	
	@RequestMapping(value=VideoSvcApi.VIDEO_SVC_PATH_ID + "/like", method=RequestMethod.POST)
	public void likeVideo(
			@PathVariable(VideoSvcApi.ID_PARAMETER) long id,
			@RequestParam("like") Principal user,
			HttpServletResponse response) {
		
		Video video = videoRepo.findOne(id);
		
		if (video == null) {
			response.setStatus(HttpStatus.NOT_FOUND.value());
			return;
		}
		
		Set<String> likedUsers = video.getLikedUsers();
		
		if (likedUsers.contains(user.getName())) {
			response.setStatus(HttpStatus.BAD_REQUEST.value());
			return;
		}
		
		likedUsers.add(user.getName());
		video.setLikedUsers(likedUsers);
		video.setLikes(likedUsers.size());
		
		videoRepo.save(video);
	}
	
	@RequestMapping(value=VideoSvcApi.VIDEO_SVC_PATH_ID + "/unlike", method=RequestMethod.POST)
	public void unlikeVideo(
			@PathVariable(VideoSvcApi.ID_PARAMETER) long id,
			@RequestParam("unlike") Principal user,
			HttpServletResponse response) {
		
		Video video = videoRepo.findOne(id);
		
		if (video == null) {
			response.setStatus(HttpStatus.NOT_FOUND.value());
			return;
		}
		
		Set<String> likedUsers = video.getLikedUsers();
		
		if (!likedUsers.contains(user.getName())) {
			response.setStatus(HttpStatus.BAD_REQUEST.value());
			return;
		}
		
		likedUsers.remove(user.getName());
		video.setLikedUsers(likedUsers);
		video.setLikes(likedUsers.size());
		
		videoRepo.save(video);
	}
	
	@RequestMapping(value=VideoSvcApi.VIDEO_TITLE_SEARCH_PATH, method=RequestMethod.GET)
	public @ResponseBody Collection<Video> findByTitle(
			@RequestParam(VideoSvcApi.TITLE_PARAMETER) String title) {
		
		return videoRepo.findByName(title);
	}
	
	@RequestMapping(value=VideoSvcApi.VIDEO_DURATION_SEARCH_PATH, method=RequestMethod.GET)
	public @ResponseBody Collection<Video> findByDurationLessThan(
			@RequestParam(VideoSvcApi.DURATION_PARAMETER) long duration) {
		
		return videoRepo.findByDurationLessThan(duration);
	}
	
	@RequestMapping(value=VideoSvcApi.VIDEO_SVC_PATH_ID + "/likedby", method=RequestMethod.GET)
	public @ResponseBody Collection<String> getUsersWhoLikedVideo(
			@PathVariable(VideoSvcApi.ID_PARAMETER) long id,
			HttpServletResponse response) {
		
		Video video = videoRepo.findOne(id);
		
		if (video == null) {
			response.setStatus(HttpStatus.NOT_FOUND.value());
			return null;
		}
		
		Set<String> users = video.getLikedUsers();
		
		return users;		
	}
}

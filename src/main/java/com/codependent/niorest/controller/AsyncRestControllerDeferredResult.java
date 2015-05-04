package com.codependent.niorest.controller;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import com.codependent.niorest.dto.Data;
import com.codependent.niorest.service.DataService;

@RestController
public class AsyncRestControllerDeferredResult {

	@Autowired
	private DataService dataService;
	
	
	private final Queue<DeferredResult<List<Data>>> responseBodyQueue = new ConcurrentLinkedQueue<DeferredResult<List<Data>>>();


	private final Queue<DeferredResult<List<Data>>> exceptionQueue = new ConcurrentLinkedQueue<DeferredResult<List<Data>>>();


	@RequestMapping("/async/data/deferred-result")
	public DeferredResult<List<Data>> deferredResult() {
		DeferredResult<List<Data>> result = new DeferredResult<List<Data>>();
		List<Data> loadData = dataService.loadData();
		result.setResult(loadData);
		this.responseBodyQueue.add(result);
		return result;
	}

	

	@RequestMapping("/async/data/deferred-result/exception")
	public DeferredResult<List<Data>> deferredResultWithException() {
		DeferredResult<List<Data>> result = new DeferredResult<List<Data>>();
		this.exceptionQueue.add(result);
		return result;
	}

	@RequestMapping("/async/data/deferred-result/timeout-value")
	public  DeferredResult<List<Data>> deferredResultWithTimeoutValue() {

		// Provide a default result in case of timeout and override the timeout value
		// set in src/main/webapp/WEB-INF/spring/appServlet/servlet-context.xml

		return new DeferredResult<List<Data>>(1000L, "Deferred result after timeout");
	}

	@Scheduled(fixedRate=2000)
	public void processQueues() {
		for (DeferredResult<List<Data>> result : this.responseBodyQueue) {
//			result.setResult("Deferred result");
			this.responseBodyQueue.remove(result);
		}
		for (DeferredResult<List<Data>> result : this.exceptionQueue) {
			result.setErrorResult(new IllegalStateException("DeferredResult error"));
			this.exceptionQueue.remove(result);
		}
		
	}

	@ExceptionHandler
	public String handleException(IllegalStateException ex) {
		return "Handled exception: " + ex.getMessage();
	}
	
	
	
}

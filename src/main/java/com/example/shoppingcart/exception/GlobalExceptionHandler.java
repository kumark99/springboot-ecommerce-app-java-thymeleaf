package com.example.shoppingcart.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ModelAndView handleResourceNotFoundException(ResourceNotFoundException ex) {
        logger.error("Resource not found: ", ex);
        ModelAndView mav = new ModelAndView();
        mav.addObject("errorMessage", ex.getMessage());
        mav.setViewName("error");
        return mav;
    }

    @ExceptionHandler(ServiceException.class)
    public ModelAndView handleServiceException(ServiceException ex) {
        logger.error("Service error: ", ex);
        ModelAndView mav = new ModelAndView();
        mav.addObject("errorMessage", "A service error occurred: " + ex.getMessage());
        mav.setViewName("error");
        return mav;
    }

    @ExceptionHandler(Exception.class)
    public ModelAndView handleException(Exception ex) {
        logger.error("An unexpected error occurred: ", ex);
        ModelAndView mav = new ModelAndView();
        mav.addObject("errorMessage", "An unexpected error occurred. Please try again later.");
        mav.setViewName("error");
        return mav;
    }
}

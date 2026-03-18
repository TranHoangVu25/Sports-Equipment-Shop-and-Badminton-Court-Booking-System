package com.thv.sport.system.controller;

import com.thv.sport.system.common.Constants;
import com.thv.sport.system.dto.response.ApiResponse;
import com.thv.sport.system.dto.response.homepage.ProductHomeResponse;
import com.thv.sport.system.service.ProductService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RestController
@RequestMapping(Constants.ApiPath.API_HOME)
@Slf4j
public class HomePageController extends BaseController{
    ProductService productService;

    @GetMapping("/home-products")
    public ResponseEntity<ApiResponse<List<ProductHomeResponse>>> getHomeProducts(
            @RequestParam("mainCategory") String mainCategory
    ){
        return productService.getTop10ProductsByMainCategory(mainCategory);
    }
}

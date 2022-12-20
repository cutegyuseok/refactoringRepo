package com.cafe.shop.product.repository;

import com.cafe.shop.mapper.ProductMapper;
import com.cafe.shop.product.dto.Product;
import com.cafe.shop.util.SearchDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ProductRepository {

    @Autowired
    ProductMapper mapper;

    public List<Product> selectProduct(SearchDto params){
        return mapper.selectProduct(params);
    }
    public int count(SearchDto params){
        return mapper.count(params);
    }

}

package com.techmatrix18.controllers.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST API and URLs for collections
 *
 * @author Alexander Kuziv makklays@gmail.com
 * @since 01-07-2025
 * @version 0.0.1
 */

@RestController
public class CollectionsController {

    @GetMapping("object-mapper")
    public String hashMap() throws JsonProcessingException {
        ObjectMapper objMapper = new ObjectMapper();

        Client client = new Client("Http", 1970);
        String json = objMapper.writeValueAsString(client);

        System.out.println(json); // {"title":"Http","year":1970}

        return json;
    }
}

class Client {
    public String title;
    public Integer year;

    public Client() {};
    public Client(String title, int year) {
        this.title = title;
        this.year = year;
    }
}


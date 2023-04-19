package org.example;

import com.google.gson.Gson;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.*;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Scanner;

public class Main {
    static final String token = "OAuth y0_AgAAAABqCF1KAADLWwAAAADhLiramVsjX4trS4akjLmsMyqyYSAmG_I";
    static CloseableHttpClient httpClient = HttpClientBuilder.create()
            .setDefaultRequestConfig(RequestConfig.custom()
                    .setConnectTimeout(5000)
                    .setSocketTimeout(30000)
                    .setRedirectsEnabled(false)
                    .build())
            .build();

    public static void main(String[] args) {
        String[] folderNames = {
                "MyImages",
                "Animals"};

        createFolder(folderNames[0]);
        createFolder(folderNames[1]);

        String uploadPath = getPathToUpload("MyImages/Cat.jpg");
        uploadFile(uploadPath, new File("smart_cat.jpg"));

        uploadPath = getPathToUpload("MyImages/Cat2.jpg");
        uploadFile(uploadPath, new File("evenSmarterCat.jpg"));

        moveFile(new File("Cat.jpg"));
        removeFile(folderNames[0], "Cat2.jpg");
    }

    private static void removeFile(String folderName, String fileName) {
        HttpDelete request = new HttpDelete("https://cloud-api.yandex.net/v1/disk/resources?path=/" + folderName + "/" + fileName);
        request.setHeader("Authorization", token);
        getHttpResponse(request);
    }

    private static void moveFile(File file) {
        HttpPost request = new HttpPost("https://cloud-api.yandex.net/v1/disk/resources/move?from=/MyImages/" + file + "&path=/Animals/" + file);
        request.setHeader("Authorization", token);
        getHttpResponse(request);
    }

    private static void uploadFile(String uploadPath, File file) {
        HttpPut request = new HttpPut(uploadPath);
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.addBinaryBody("file", file, ContentType.APPLICATION_OCTET_STREAM, file.getName());
        HttpEntity entity = builder.build();
        request.setEntity(entity);
        getHttpResponse(request);
    }

    private static String getPathToUpload(String path) {
        HttpGet request = new HttpGet("https://cloud-api.yandex.net/v1/disk/resources/upload?path=" + path);
        request.setHeader("Authorization", token);
        try (CloseableHttpResponse response = httpClient.execute(request)) {
//            Scanner scanner = new Scanner(response.getEntity().getContent());
//            while(scanner.hasNext()){
//                System.out.println(scanner.nextLine());
//            }
            Gson gson = new Gson();
            Map map = gson.fromJson(EntityUtils.toString(response.getEntity()), Map.class);
            return map.get("href").toString();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void createFolder(String name) {
        HttpPut request = new HttpPut("https://cloud-api.yandex.net/v1/disk/resources?path=" + name);
        request.setHeader("Authorization", token);
        getHttpResponse(request);
    }

    private static void getHttpResponse(HttpRequestBase request) {
        try (CloseableHttpResponse response = httpClient.execute(request)) {
            Scanner scanner = new Scanner(response.getEntity().getContent());
            while (scanner.hasNext()) {
                System.out.println(scanner.nextLine());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}